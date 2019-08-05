package fho.kdvs.settings

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
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

    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)
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

                spinner.setSelection(position)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        kdvsPreferences.streamUrl = when (position) {
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
                val position = when (kdvsPreferences.notificationTime) {
                    5 -> 0
                    10 -> 1
                    15 -> 2
                    20 -> 3
                    else -> 1
                }

                spinner.setSelection(position)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        kdvsPreferences.notificationTime = when (position) {
                            0 -> 5
                            1 -> 10
                            2 -> 15
                            3 -> 20
                            else -> 10
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

                spinner.setSelection(position)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        kdvsPreferences.fundraiserWindow = when (position) {
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

                spinner.setSelection(position)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        kdvsPreferences.scrapeFrequency = when (position) {
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

                spinner.setSelection(position)
                spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        kdvsPreferences.theme = KdvsPreferences.Theme.values()[position].value
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        if (kdvsPreferences.dataSaverMode == true) {
            dataSaverSwitch.setOnCheckedChangeListener(null)
            dataSaverSwitch.isChecked = true
        }

        dataSaverSwitch.setOnCheckedChangeListener { _, isChecked -> kdvsPreferences.dataSaverMode = isChecked }

        setDownloadLocation.setOnClickListener { viewModel?.setDownloadFolder(activity) }
        refresh.setOnClickListener { viewModel?.refreshData() }
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
                            Timber.d("Preferences reset")
                        })
                    .setNegativeButton(android.R.string.no, null).show()
            }

        }
    }
}
