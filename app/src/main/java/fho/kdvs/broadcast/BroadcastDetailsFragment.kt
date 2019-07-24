package fho.kdvs.broadcast

import android.app.DownloadManager
import android.app.DownloadManager.EXTRA_DOWNLOAD_ID
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentBroadcastDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.MainActivity
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import kotlinx.android.synthetic.main.fragment_broadcast_details.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import javax.inject.Inject


class BroadcastDetailsFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: BroadcastDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private var tracksAdapter: BroadcastTracksAdapter? = null

    private val broadcastId: Int by lazy {
        arguments?.let { BroadcastDetailsFragmentArgs.fromBundle(it) }?.broadcastId
            ?: throw IllegalArgumentException("Should have passed a broadcastId to BroadcastDetailsFragment")
    }

    private val showId: Int by lazy {
        arguments?.let { BroadcastDetailsFragmentArgs.fromBundle(it) }?.showId
            ?: throw IllegalArgumentException("Should have passed a showId to BroadcastDetailsFragment")
    }

    private var downloadId: Long? = null
    private lateinit var downloadManager: DownloadManager

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)

            if (downloadId != null && downloadId == id) {
                Toast.makeText(activity as? MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(BroadcastDetailsViewModel::class.java)
            .also { it.initialize(showId, broadcastId) }

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        context?.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        subscribeToViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()

        context?.unregisterReceiver(onDownloadComplete)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentBroadcastDetailsBinding.inflate(inflater, container, false)
        binding.apply {
            vm = viewModel
            dateFormatter = TimeHelper.uiDateFormatter
        }

        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.broadcast.value == null)
            progressBar.visibility = View.VISIBLE

        tracksAdapter = BroadcastTracksAdapter(viewModel, sharedViewModel) {
            Timber.d("Clicked ${it.item}")
            viewModel.onClickTrack(this.findNavController(), it.item)
        }

        trackRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = tracksAdapter
        }

        archivePlayButton.setOnClickListener {
            val broadcast = viewModel.broadcast.value
            val show = viewModel.show.value

            broadcast?.let {
                show?.let {
                    sharedViewModel.playPastBroadcast(broadcast, show)
                }
            }
        }

        downloadSwitch.setOnCheckedChangeListener { _, isChecked ->
            // skip event firing from programmatic checkedChange
            if (downloadSwitch.tag != null) {
                downloadSwitch.tag = null
                return@setOnCheckedChangeListener
            }

            viewModel.broadcast.value?.let { broadcast ->
                viewModel.show.value?.let { show ->
                    val folder = sharedViewModel.getDestinationFolder()
                    folder?.let {
                        val title = sharedViewModel.getBroadcastDownloadTitle(broadcast, show)
                        val filename = title + sharedViewModel.getBroadcastFileExtension()
                        val file = sharedViewModel.getDestinationFile(filename)

                        when (isChecked) {
                            true -> {
                                (activity as? MainActivity)?.let { activity ->
                                    if (activity.haveStoragePermission()) {
                                        val url = URLs.archiveForBroadcast(broadcast)

                                        url?.let {
                                            if (!folder.exists())
                                                folder.mkdir()

                                            val request = sharedViewModel.makeDownloadRequest(url, title, file)

                                            downloadManager = context?.getSystemService(DOWNLOAD_SERVICE)
                                                    as DownloadManager

                                            downloadId = downloadManager.enqueue(request)
                                        }
                                    }
                                }
                            }
                            false -> {
                                val id = downloadId

                                if (::downloadManager.isInitialized && id != null) {
                                    downloadManager.remove(id)
                                }

                                sharedViewModel.deleteFile(file)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun subscribeToViewModel() {
        val fragment = this

        viewModel.broadcast.observe(fragment, Observer { broadcast ->
            Timber.d("Got broadcast: $broadcast")
            setPlaybackViewsAndHideProgressBar(broadcast)

            viewModel.show.observe(fragment, Observer { show ->
                val folder = sharedViewModel.getDestinationFolder()

                if (folder != null && folder.exists()) {
                    val title = sharedViewModel.getBroadcastDownloadTitle(broadcast, show)
                    val files = folder.listFiles()

                    files?.let {
                        if (it.count{ f -> f.name.contains(title)} > 0) {
                            setSwitchForDownloadedBroadcast()
                        }
                    }
                }
            })
        })

        viewModel.tracksWithFavorites.observe(fragment, Observer { (tracks, _) ->
            Timber.d("Got tracks: $tracks with favorites")

            noTracksMessage.visibility = if (tracks.isEmpty()) View.VISIBLE
                else View.GONE

            tracksAdapter?.onTracksChanged(tracks)
        })
    }

    private fun setSwitchForDownloadedBroadcast() {
        downloadSwitch.tag = "TAG"
        downloadSwitch.isChecked = true
    }

    private fun setPlaybackViewsAndHideProgressBar(broadcast: BroadcastEntity) {
        doAsync {
            val isConnAvailable = HttpHelper.isConnectionAvailable(URLs.archiveForBroadcast(broadcast))

            uiThread {
                if (!isConnAvailable) {
                    archivePlayButton?.let { it.visibility = View.GONE }
                    downloadSwitch?.let { it.visibility = View.GONE }
                    downloaded?.let { it.visibility = View.GONE }
                }

                progressBar?.let { it.visibility = View.GONE }
            }
        }
    }
}