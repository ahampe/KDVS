package fho.kdvs.broadcast

import android.app.Activity
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.FileObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.R
import fho.kdvs.api.service.SpotifyService
import fho.kdvs.databinding.FragmentBroadcastDetailsBinding
import fho.kdvs.dialog.BinaryChoiceDialogFragment
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.MainActivity
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.enums.ThirdPartyService
import fho.kdvs.global.extensions.collapseExpand
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.*
import kotlinx.android.synthetic.main.fragment_broadcast_details.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.File
import javax.inject.Inject

const val DOWNLOAD_ICON = "download"
const val DELETE_ICON = "delete"

class BroadcastDetailsFragment : BaseFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: BroadcastDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    @Inject
    lateinit var spotifyService: SpotifyService

    private var tracksAdapter: BroadcastTracksAdapter? = null

    private val broadcastId: Int by lazy {
        arguments?.let { BroadcastDetailsFragmentArgs.fromBundle(it) }?.broadcastId
            ?: throw IllegalArgumentException("Should have passed a broadcastId to BroadcastDetailsFragment")
    }

    private val showId: Int by lazy {
        arguments?.let { BroadcastDetailsFragmentArgs.fromBundle(it) }?.showId
            ?: throw IllegalArgumentException("Should have passed a showId to BroadcastDetailsFragment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(BroadcastDetailsViewModel::class.java)
            .also { it.initialize(showId, broadcastId) }

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentBroadcastDetailsBinding.inflate(inflater, container, false)

        binding.apply {
            vm = viewModel
            dateFormatter = TimeHelper.uiDateFormatter
        }

        binding.lifecycleOwner = this

        subscribeToViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(detailsRoot) // TODO: this renders beneath motionscene stuff

        tracksAdapter = BroadcastTracksAdapter(viewModel, sharedViewModel) {
            Timber.d("Clicked ${it.item}")
            viewModel.onClickTrack(this.findNavController(), it.item)
        }

        trackRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = tracksAdapter
        }

        archivePlayButton.setOnClickListener {
            viewModel.showWithBroadcast.observe(this, Observer { (show, broadcast) ->
                sharedViewModel.preparePastBroadcastForPlaybackAndPlay(broadcast, show, requireActivity())
            })
        }

        downloadDeleteIcon.setOnClickListener { icon ->
            viewModel.showWithBroadcast.observe(this, Observer { (show, broadcast) ->
                val folder = sharedViewModel.getDownloadFolder()

                folder?.let {
                    if (icon.tag == DOWNLOAD_ICON) {
                        if (downloadBroadcast(broadcast, show, folder))
                            setDownloadingIcon()
                    } else if (icon.tag == DELETE_ICON) {
                        displayDialog()
                    }
                }
            })
        }

        broadcast_description.setOnClickListener {
            (it as? TextView).collapseExpand(5)
        }

        broadcast_parent_show.setOnClickListener {
            onClickHeader()
        }

        broadcast_date.setOnClickListener {
            onClickHeader()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCodes.DOWNLOAD_DELETE_DIALOG -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.showWithBroadcast.observe(this, Observer { (show, broadcast) ->
                        deleteBroadcast(broadcast, show)
                        flipIcon()
                        Toast.makeText(requireContext(),  "Download deleted", Toast.LENGTH_SHORT)
                            .show()
                    })
                }
            }
            RequestCodes.SPOTIFY_EXPORT_BROADCAST -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (sharedViewModel.isSpotifyAuthVoidOrExpired()) {
                        sharedViewModel.loginSpotify(requireActivity())
                        sharedViewModel.spotToken.observe(viewLifecycleOwner, Observer { token ->
                            token?.let {
                                exportTracksToSpotify(token)
                            }
                        })
                    } else {
                        exportTracksToSpotify(kdvsPreferences.spotifyAuthToken as String)
                    }
                }
            }
            RequestCodes.YOUTUBE_EXPORT_BROADCAST -> {
                exportTracksToYouTube()
            }
        }
    }

    private fun subscribeToViewModel() {
        val fragment = this

        viewModel.showWithBroadcast.observe(this, Observer { (show, broadcast) ->
            if (broadcast.description?.trim().isNullOrBlank())
                description_container?.visibility = View.GONE

            val title = sharedViewModel.getBroadcastDownloadTitle(broadcast, show)
            observeDownloadFolder(title)

            when {
                sharedViewModel.isBroadcastDownloaded(broadcast, show) -> {
                    setDownloadViewsVisible()

                    enableDeleteIcon()

                    LoadScreen.hideLoadScreen(detailsRoot)
                }
                sharedViewModel.isBroadcastDownloading(broadcast, show) -> {
                    setDownloadViewsVisible()

                    setDownloadingIcon()

                    LoadScreen.hideLoadScreen(detailsRoot)
                }
                else -> setPlaybackViewsAndHideProgressBar(broadcast)
            }
        })

        viewModel.tracksWithFavorites.observe(fragment, Observer { (tracks, _) ->
            Timber.d("Got tracks: $tracks with liveFavorites")

            noTracksMessage.visibility = if (tracks.isEmpty()) View.VISIBLE
                else View.GONE

            spotifyExportIconBroadcast.visibility = if (noTracksMessage.visibility == View.VISIBLE) View.GONE
                else View.VISIBLE

            youtubeExportIconBroadcast.visibility = spotifyExportIconBroadcast.visibility

            tracksAdapter?.onTracksChanged(tracks)

            spotifyExportIconBroadcast?.setOnClickListener {
                sharedViewModel.onClickExportIcon(
                    this,
                    RequestCodes.SPOTIFY_EXPORT_BROADCAST,
                    ThirdPartyService.SPOTIFY
                )
            }

            youtubeExportIconBroadcast?.setOnClickListener {
                sharedViewModel.onClickExportIcon(
                    this,
                    RequestCodes.YOUTUBE_EXPORT_BROADCAST,
                    ThirdPartyService.YOUTUBE
                )
            }
        })
    }

    private fun exportTracksToSpotify(token: String) {
        var hasExecuted = false

        viewModel.showWithBroadcast.observe(this, Observer { (show, broadcast) ->
            viewModel.tracksLiveData.observe(this, Observer { tracks ->
                if (!hasExecuted) {
                    val jobs = mutableListOf<Job?>() // We must fetch data prior to export
`
                    tracks.forEach {
                        jobs.add(sharedViewModel.fetchThirdPartyDataForTrack(it))
                    }

                    launch {
                        jobs.filterNotNull()
                            .joinAll()

                        val uris = tracks.mapNotNull { t -> t.spotifyTrackUri }
                        val title = sharedViewModel.getBroadcastDownloadUiTitle(broadcast, show)

                        ExportManagerSpotify(
                            context = requireContext(),
                            spotifyService = spotifyService,
                            trackUris = uris,
                            userToken = token,
                            playlistTitle = title
                        ).getExportPlaylistUri()
                            ?.let {
                                sharedViewModel.openSpotify(requireContext(), it)
                            }
                    }

                    hasExecuted = true
                }
            })
        })
    }

    private fun exportTracksToYouTube() {
        viewModel.tracksLiveData.observe(this, Observer { tracks ->
            val ids = tracks.mapNotNull { t -> t.youTubeId }

            sharedViewModel.exportVideosToYouTubePlaylist(requireContext(), ids)
        })
    }

    private fun downloadBroadcast(broadcast: BroadcastEntity, show: ShowEntity, folder: File): Boolean {
        if (kdvsPreferences.offlineMode == true) {
            sharedViewModel.makeOfflineModeToast(activity)
            return false
        }

        val title = sharedViewModel.getBroadcastDownloadTitle(broadcast, show)
        val filename = sharedViewModel.getDownloadingFilename(title)

        (activity as? MainActivity)?.let { activity ->
            if (activity.isStoragePermissionGranted()) {
                val url = URLs.archiveForBroadcast(broadcast)

                url?.let {
                    if (!folder.exists())
                        folder.mkdirs()

                    try {
                        val request = sharedViewModel.makeDownloadRequest(url, title, filename)

                        val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE)
                                as DownloadManager

                        downloadManager.enqueue(request)

                        Toast.makeText(
                            activity as? MainActivity, "Download started", Toast.LENGTH_SHORT
                        ).show()

                        return true
                    } catch (e: Exception) {
                        Timber.e("Error downloading broadcast: ${e.message}")

                        Toast.makeText(
                            activity as? MainActivity, "Error downloading broadcast", Toast.LENGTH_SHORT
                        ).show()

                        return false
                    }
                }
            }
        }

        return false
    }

    /** Update UI reactively to match state of download. */
    private fun observeDownloadFolder(title: String) {
        val folder = sharedViewModel.getDownloadFolder()
        val downloadingFilename = sharedViewModel.getDownloadingFilename(title)

        val observer = object: FileObserver(folder?.path) {
            override fun onEvent(event: Int, filename: String?) {
                filename?.let {
                    if (it == downloadingFilename) {
                        when (event) {
                            MOVED_FROM -> { // capture file rename upon completion
                                enableDeleteIcon()
                            }
                        }
                    }
                }
            }
        }

        observer.startWatching()
    }

    private fun deleteBroadcast(broadcast: BroadcastEntity, show: ShowEntity) {
        val file = sharedViewModel.getDownloadFileForBroadcast(broadcast, show)

        file?.let {
            sharedViewModel.deleteFile(file)
        }
    }

    // TODO: The UI thread appears to get deadlocked sometimes after this?
    private fun setPlaybackViewsAndHideProgressBar(broadcast: BroadcastEntity) {
        doAsync {
            if (HttpHelper.isConnectionAvailable(URLs.archiveForBroadcast(broadcast))) {
                context?.runOnUiThread {
                    setDownloadViewsVisible()
                }
            }

            context?.runOnUiThread {
                LoadScreen.hideLoadScreen(detailsRoot)
            }
        }
    }

    private fun displayDialog() {
        val dialog = BinaryChoiceDialogFragment()
        val args = Bundle()

        args.putString("title", "Delete")
        args.putString("message", "Delete broadcast download?")

        dialog.arguments = args
        dialog.setTargetFragment(this@BroadcastDetailsFragment, RequestCodes.DOWNLOAD_DELETE_DIALOG)
        dialog.show(requireFragmentManager(), "tag")
    }

    private fun setDownloadingIcon() {
        downloadDeleteIcon.alpha = 0.25f
        downloadDeleteIcon.isEnabled = false
    }

    private fun flipIcon() {
        if (downloadDeleteIcon.tag == DOWNLOAD_ICON) {
            enableDeleteIcon()
        } else {
            enableDownloadIcon()
        }
    }

    private fun enableDeleteIcon() {
        downloadDeleteIcon?.let {
            it.setImageDrawable(it.resources.getDrawable(R.drawable.ic_delete_forever_white_24dp, context?.theme))
            it.tag = DELETE_ICON
            it.isEnabled = true
            it.alpha = 1f
        }
    }

    private fun enableDownloadIcon() {
        downloadDeleteIcon?.let {
            it.setImageDrawable(it.resources.getDrawable(R.drawable.ic_file_download_white_24dp, context?.theme))
            it.tag = DOWNLOAD_ICON
            it.isEnabled = true
            it.alpha = 1f
        }
    }

    private fun setDownloadViewsVisible() {
        Timber.d("Download or stream found for broadcast.")
        archivePlayButton?.let { it.visibility = View.VISIBLE }
        downloadDeleteIcon?.let { it.visibility = View.VISIBLE }
    }

    private fun onClickHeader() {
        val navAction = BroadcastDetailsFragmentDirections
            .actionBroadcastDetailsFragmentToShowDetailsFragment(showId)
        if (findNavController().currentDestination?.id == R.id.broadcastDetailsFragment)
            findNavController().navigate(navAction)
    }
}
