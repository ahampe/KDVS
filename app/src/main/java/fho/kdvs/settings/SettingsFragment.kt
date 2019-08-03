package fho.kdvs.settings

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import fho.kdvs.R
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

        val defaultPath = viewModel.getDownloadFolder()?.toURI()?.path ?: ""

        context?.let {
            val liveSharedPreferences = kdvsPreferences.LiveSharedPreferences(kdvsPreferences.preferences)

            liveSharedPreferences.getString("${kdvsPreferences.downloadPath}}", defaultPath)
                .observe(this, Observer<String> { path ->
                Timber.d("Download path changed to $path")
                downloadPath.text = path
            })

            ArrayAdapter.createFromResource(
                it,
                R.array.codecs_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                val position = when (kdvsPreferences.streamUrl) {
                    URLs.LIVE_OGG -> 0
                    URLs.LIVE_AAC -> 1
                    URLs.LIVE_MP3 -> 2
                    else -> 0
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                codecSpinner.adapter = adapter
                codecSpinner.setSelection(position)
                codecSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
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

            ArrayAdapter.createFromResource(
                it,
                R.array.notification_time_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                val position = when (kdvsPreferences.notificationTime) {
                    5 -> 0
                    10 -> 1
                    15 -> 2
                    20 -> 3
                    else -> 1
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                notificationSpinner.adapter = adapter
                notificationSpinner.setSelection(position)
                notificationSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
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

            ArrayAdapter.createFromResource(
                it,
                R.array.fundraiser_window_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                val position = when (kdvsPreferences.fundraiserWindow) {
                    1 -> 0
                    2 -> 1
                    3 -> 2
                    else -> 1
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                fundraiserSpinner.adapter = adapter
                fundraiserSpinner.setSelection(position)
                fundraiserSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
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

            ArrayAdapter.createFromResource(
                it,
                R.array.frequency_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                val position = when (kdvsPreferences.scrapeFrequency) {
                    WebScraperManager.DEFAULT_SCRAPE_FREQ -> 0
                    WebScraperManager.DAILY_SCRAPE_FREQ -> 1
                    WebScraperManager.WEEKLY_SCRAPE_FREQ -> 2
                    else -> 0
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                frequencySpinner.adapter = adapter
                frequencySpinner.setSelection(position)
                frequencySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
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

            ArrayAdapter.createFromResource(
                it,
                R.array.themes_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                val position = KdvsPreferences.Theme.values()
                    .find { t -> t.value == kdvsPreferences.theme }?.value ?: 0

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                themeSpinner.adapter = adapter
                themeSpinner.setSelection(position)
                themeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        kdvsPreferences.theme = KdvsPreferences.Theme.values()[position].value
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        if (kdvsPreferences.allowedOverMetered == true) {
            meteredSwitch.setOnCheckedChangeListener(null)
            meteredSwitch.isChecked = true
        }

        if (kdvsPreferences.allowedOverRoaming == true) {
            roamingSwitch.setOnCheckedChangeListener(null)
            roamingSwitch.isChecked = true
        }

        meteredSwitch.setOnCheckedChangeListener { _, isChecked -> kdvsPreferences.allowedOverMetered = isChecked }
        roamingSwitch.setOnCheckedChangeListener { _, isChecked -> kdvsPreferences.allowedOverRoaming = isChecked }

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

        downloadPath.text = kdvsPreferences.downloadPath ?: defaultPath
        if (downloadPath.text.isNullOrBlank()) downloadPath.visibility = View.GONE
    }
}
