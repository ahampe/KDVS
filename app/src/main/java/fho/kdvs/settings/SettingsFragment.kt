package fho.kdvs.settings

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentSettingsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.android.synthetic.main.fragment_settings.*
import timber.log.Timber
import javax.inject.Inject


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
    private var downloadPath: String? = null

    private lateinit var viewModel: SharedViewModel

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
        downloadPath = kdvsPreferences.downloadPath

        requireActivity().onBackPressedDispatcher.addCallback(
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isChanged()) {
                        context?.let {
                            AlertDialog.Builder(it)
                                .setTitle("Exit")
                                .setMessage("Return without saving changes?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes,
                                    DialogInterface.OnClickListener { dialog, _ ->
                                        dialog.dismiss()
                                        super.setEnabled(false)
                                        fragmentManager?.popBackStack()
                                    })
                                .setNegativeButton(android.R.string.no, null).show()
                        }
                    } else {
                        super.setEnabled(false)
                        fragmentManager?.popBackStack()
                    }
                }
            }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                    else -> 0
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        streamUrl = when (position) {
                            0 -> URLs.LIVE_OGG
                            1 -> URLs.LIVE_AAC
                            2 -> URLs.LIVE_MP3
                            else -> URLs.LIVE_OGG
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) { }
                }
            }

            notificationSpinner?.let { spinner ->
                val position = when (kdvsPreferences.alarmNoticeInterval) {
                    0L -> 0
                    5L -> 1
                    10L -> 2
                    15L -> 3
                    else -> 0
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        alarmNoticeInterval = when (position) {
                            0 -> 0
                            1 -> 5
                            2 -> 10
                            3 -> 15
                            else -> 0
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) { }
                }
            }

            fundraiserSpinner?.let { spinner ->
                val position = when (kdvsPreferences.fundraiserWindow) {
                    1 -> 0
                    2 -> 1
                    3 -> 2
                    else -> 1
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        fundraiserWindow = when (position) {
                            0 -> 1
                            1 -> 2
                            2 -> 3
                            else -> 2
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) { }
                }
            }

            frequencySpinner?.let { spinner ->
                val position = when (kdvsPreferences.scrapeFrequency) {
                    WebScraperManager.DEFAULT_SCRAPE_FREQ -> 0
                    WebScraperManager.DAILY_SCRAPE_FREQ -> 1
                    WebScraperManager.WEEKLY_SCRAPE_FREQ -> 2
                    else -> 0
                }

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
                    .find { t -> t.value == kdvsPreferences.theme }?.value ?: 0

                spinner.setSelection(position, false)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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

        val offlineSwitchChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            offlineMode = isChecked
        }

        offlineSwitch.setOnCheckedChangeListener{ _, isChecked ->
            offlineMode = isChecked
        }

        setDownloadLocation.setOnClickListener { viewModel?.setDownloadFolder(activity) }

        refresh.setOnClickListener {
            viewModel?.refreshData()

            Toast.makeText(activity,
                "Information updated.",
                Toast.LENGTH_SHORT)
                .show()
        }

        contactDevs.setOnClickListener { viewModel?.composeEmail(contactDevs, URLs.CONTACT_EMAIL) }

        resetSettings.setOnClickListener {
            context?.let {
                AlertDialog.Builder(it)
                    .setTitle("Reset")
                    .setMessage("Do you want to reset settings back to default?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes,
                        DialogInterface.OnClickListener { _, _ ->
                            kdvsPreferences.clearAll()

                            codecSpinner.setSelection(0)
                            notificationSpinner.setSelection(1)

                            offlineSwitch.setOnCheckedChangeListener(null)
                            offlineSwitch.isChecked = false
                            offlineSwitch.setOnCheckedChangeListener(offlineSwitchChangeListener)

                            fundraiserSpinner.setSelection(1)
                            frequencySpinner.setSelection(0)
                            themeSpinner.setSelection(0)
                            
                            Timber.d("Preferences reset")
                        })
                    .setNegativeButton(android.R.string.no, null).show()
            }
        }

        saveButton.setOnClickListener {
            save()
        }
    }

    // Note: tempDownloadPath is set through a MainActivity callback.
    private fun save() {
        // If flipped to offlineMode, stop current live playback
        viewModel.isLiveNow.observe(this, Observer {live ->
            if (live == true && offlineMode == true && kdvsPreferences.offlineMode != true)
                viewModel.stopPlayback()
        })

        kdvsPreferences.streamUrl = streamUrl
        kdvsPreferences.alarmNoticeInterval = alarmNoticeInterval
        kdvsPreferences.fundraiserWindow = fundraiserWindow
        kdvsPreferences.scrapeFrequency = scrapeFrequency
        kdvsPreferences.theme = theme
        kdvsPreferences.offlineMode = offlineMode
        kdvsPreferences.downloadPath = kdvsPreferences.tempDownloadPath
    }

    private fun isChanged() = streamUrl != kdvsPreferences.streamUrl ||
        alarmNoticeInterval != kdvsPreferences.alarmNoticeInterval ||
        fundraiserWindow != kdvsPreferences.fundraiserWindow ||
        scrapeFrequency != kdvsPreferences.scrapeFrequency ||
        theme != kdvsPreferences.theme ||
        offlineMode != kdvsPreferences.offlineMode ||
        kdvsPreferences.tempDownloadPath != kdvsPreferences.downloadPath
}
