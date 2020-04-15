package fho.kdvs.modules.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentSettingsBinding
import fho.kdvs.dialog.BinaryChoiceDialogFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.RequestCodes.SETTINGS_DIALOG
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject


const val DEFAULT_CODEC_POS = 0
const val DEFAULT_NOTIFICATION_POS = 0
const val DEFAULT_FUNDRAISER_POS = 1
const val DEFAULT_FREQUENCY_POS = 0
const val DEFAULT_THEME_POS = 0

class SettingsFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    private var streamUrl: String? = null
    private var alarmNoticeInterval: Long? = null
    private var fundraiserWindow: Int? = null
    private var scrapeFrequency: Long? = null
    private var theme: Int? = null
    private var offlineMode: Boolean? = null

    private lateinit var viewModel: SharedViewModel

    private val offlineSwitchChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            offlineMode = isChecked
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        streamUrl = kdvsPreferences.streamUrl
        alarmNoticeInterval = kdvsPreferences.alarmNoticeInterval
        fundraiserWindow = kdvsPreferences.fundraiserWindow
        scrapeFrequency = kdvsPreferences.scrapeFrequency
        theme = kdvsPreferences.theme
        offlineMode = kdvsPreferences.offlineMode

        // TODO: extend this to navbar press from settings frag
        requireActivity().onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isChanged()) {
                        displayDialog()
                    } else {
                        fragmentManager?.popBackStack()
                    }
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
            .apply { vm = viewModel }
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            codecSpinner?.let { spinner ->
                val position = when (kdvsPreferences.streamUrl) {
                    URLs.LIVE_OGG -> 0
                    URLs.LIVE_AAC -> 1
                    URLs.LIVE_MP3 -> 2
                    else -> DEFAULT_CODEC_POS
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        streamUrl = when (position) {
                            0 -> URLs.LIVE_OGG
                            1 -> URLs.LIVE_AAC
                            2 -> URLs.LIVE_MP3
                            else -> URLs.LIVE_OGG
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            notificationSpinner?.let { spinner ->
                val position = when (kdvsPreferences.alarmNoticeInterval) {
                    0L -> 0
                    5L -> 1
                    10L -> 2
                    15L -> 3
                    else -> DEFAULT_NOTIFICATION_POS
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        alarmNoticeInterval = when (position) {
                            0 -> 0
                            1 -> 5
                            2 -> 10
                            3 -> 15
                            else -> 0
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            fundraiserSpinner?.let { spinner ->
                val position = when (kdvsPreferences.fundraiserWindow) {
                    1 -> 0
                    2 -> 1
                    3 -> 2
                    else -> DEFAULT_FUNDRAISER_POS
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        fundraiserWindow = when (position) {
                            0 -> 1
                            1 -> 2
                            2 -> 3
                            else -> 2
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            frequencySpinner?.let { spinner ->
                val position = when (kdvsPreferences.scrapeFrequency) {
                    WebScraperManager.DEFAULT_SCRAPE_FREQ -> 0
                    WebScraperManager.DAILY_SCRAPE_FREQ -> 1
                    WebScraperManager.WEEKLY_SCRAPE_FREQ -> 2
                    else -> DEFAULT_FREQUENCY_POS
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        scrapeFrequency = when (position) {
                            0 -> WebScraperManager.DEFAULT_SCRAPE_FREQ
                            1 -> WebScraperManager.DAILY_SCRAPE_FREQ
                            2 -> WebScraperManager.WEEKLY_SCRAPE_FREQ
                            else -> 0
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            themeSpinner?.let { spinner ->
                val position = KdvsPreferences.Theme.values()
                    .find { t -> t.value == kdvsPreferences.theme }?.value ?: DEFAULT_THEME_POS

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        theme = KdvsPreferences.Theme.values()[position].value
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        if (kdvsPreferences.offlineMode == true) {
            offlineSwitch.setOnCheckedChangeListener(null)
            offlineSwitch.isChecked = true
        }

        offlineSwitch.setOnCheckedChangeListener(offlineSwitchChangeListener)

        refresh.setOnClickListener {
            viewModel.refreshData()

            Toast.makeText(
                activity,
                "Information updated.",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        contactDevs.setOnClickListener { viewModel.composeEmail(contactDevs, URLs.CONTACT_EMAIL) }

        resetSettings.setOnClickListener {
            reset()
        }

        saveButton.setOnClickListener {
            save()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SETTINGS_DIALOG -> {
                if (resultCode == Activity.RESULT_OK)
                    requireFragmentManager().popBackStack()
            }
        }
    }

    private fun reset() {
        codecSpinner.setSelection(DEFAULT_CODEC_POS)
        notificationSpinner.setSelection(DEFAULT_NOTIFICATION_POS)
        fundraiserSpinner.setSelection(DEFAULT_FUNDRAISER_POS)
        frequencySpinner.setSelection(DEFAULT_FREQUENCY_POS)
        themeSpinner.setSelection(DEFAULT_THEME_POS)

        offlineSwitch.setOnCheckedChangeListener(null)
        offlineSwitch.isChecked = false
        offlineSwitch.setOnCheckedChangeListener(offlineSwitchChangeListener)

        streamUrl = null
        fundraiserWindow = null
        scrapeFrequency = null
        theme = null
        offlineMode = null
    }

    private fun save() {
        // If flipped to offlineMode, stop current live playback
        viewModel.isLiveNow.observe(this, Observer { live ->
            if (live == true && offlineMode == true && kdvsPreferences.offlineMode != true)
                viewModel.stopPlayback()
        })

        // If notification window is changed, re-initialize alarms
        if (alarmNoticeInterval != kdvsPreferences.alarmNoticeInterval) {
            viewModel.reRegisterAlarmsAndUpdatePreference(alarmNoticeInterval)
        }

        kdvsPreferences.streamUrl = streamUrl
        kdvsPreferences.fundraiserWindow = fundraiserWindow
        kdvsPreferences.scrapeFrequency = scrapeFrequency
        kdvsPreferences.theme = theme
        kdvsPreferences.offlineMode = offlineMode

        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT)
            .show()
    }

    private fun isChanged() = streamUrl != kdvsPreferences.streamUrl ||
            alarmNoticeInterval != kdvsPreferences.alarmNoticeInterval ||
            fundraiserWindow != kdvsPreferences.fundraiserWindow ||
            scrapeFrequency != kdvsPreferences.scrapeFrequency ||
            theme != kdvsPreferences.theme ||
            offlineMode != kdvsPreferences.offlineMode

    private fun displayDialog() {
        val dialog = BinaryChoiceDialogFragment()
        val args = Bundle()

        args.putString("title", "Discard changes")
        args.putString("message", "Leave without saving?")

        dialog.arguments = args
        dialog.setTargetFragment(this@SettingsFragment, SETTINGS_DIALOG)
        dialog.show(requireFragmentManager(), "tag")
    }
}
