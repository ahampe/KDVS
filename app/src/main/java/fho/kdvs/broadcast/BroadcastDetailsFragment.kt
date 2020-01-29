package fho.kdvs.broadcast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import fho.kdvs.dialog.ExportLoadingDialog
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.enums.ThirdPartyService
import fho.kdvs.global.export.SpotifyExportManager
import fho.kdvs.global.extensions.collapseExpand
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.ui.MaskingLoadScreen
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.util.RequestCodes
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import kotlinx.android.synthetic.main.fragment_broadcast_details.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class BroadcastDetailsFragment : BaseFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: BroadcastDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    @Inject
    lateinit var spotifyService: SpotifyService

    private lateinit var loadScreen: MaskingLoadScreen

    private lateinit var spotifyExportManager: SpotifyExportManager

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        loadScreen = MaskingLoadScreen(
            WeakReference(
                detailsRoot
            )
        ).apply {
            display()
        }

        spotifyExportManager = SpotifyExportManager(
            requireActivity(),
            spotifyService,
            kdvsPreferences,
            sharedViewModel
        )

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
                sharedViewModel.preparePastBroadcastForPlaybackAndPlay(
                    broadcast,
                    show,
                    requireActivity()
                )
            })
        }

        downloadDeleteIcon.setOnClickListener { icon ->
            viewModel.showWithBroadcast.observe(this, Observer { (show, broadcast) ->
                val folder = sharedViewModel.getDownloadFolder()

                folder?.let {
                    if (icon.tag == DOWNLOAD_ICON) {
                        if (sharedViewModel.downloadBroadcast(
                                requireActivity(),
                                broadcast,
                                show,
                                folder
                            )
                        ) {
                            setDownloadingIcon()
                            sharedViewModel.addBroadcastFavorite(broadcast)
                        }
                    } else if (icon.tag == DELETE_ICON) {
                        displayDialog()
                    }
                }

                // Downloading a broadcast also adds it to favorites
                if (broadcastFavoriteButton.tag == 0) {
                    sharedViewModel.onClickBroadcastFavorite(broadcastFavoriteButton, broadcast)
                }
            })
        }

        broadcastFavoriteButton.setOnClickListener {
            viewModel.broadcastLiveData.observe(this, Observer { broadcast ->
                sharedViewModel.onClickBroadcastFavorite(it, broadcast)
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
                        sharedViewModel.deleteBroadcast(broadcast, show)
                        flipIcon()
                        Toast.makeText(requireContext(), "Download deleted", Toast.LENGTH_SHORT)
                            .show()
                    })
                }
            }
            RequestCodes.SPOTIFY_EXPORT_BROADCAST -> {
                if (resultCode == Activity.RESULT_OK) {
                    spotifyExportManager.loginIfNecessary()
                    exportTracksToSpotify()
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

            // Update UI reactively to match state of download
            sharedViewModel.callOnFileEventForFilename(
                title,
                ::enableDeleteIcon,
                ::enableDownloadIcon
            )

            when {
                sharedViewModel.isBroadcastDownloaded(broadcast, show) -> {
                    setDownloadViewsVisible()

                    enableDeleteIcon()

                    loadScreen.hide()
                }
                sharedViewModel.isBroadcastDownloading(broadcast, show) -> {
                    setDownloadViewsVisible()

                    setDownloadingIcon()

                    loadScreen.hide()
                }
                else -> setPlaybackViewsAndHideProgressBar(broadcast)
            }
        })

        viewModel.tracksWithFavorites.observe(fragment, Observer { (tracks, _) ->
            Timber.d("Got tracks: $tracks with liveFavorites")

            noTracksMessage.visibility = if (tracks.isEmpty()) View.VISIBLE
            else View.GONE

            spotifyExportIconBroadcast.visibility =
                if (noTracksMessage.visibility == View.VISIBLE) View.GONE
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

        viewModel.broadcastFavoriteLiveData.observe(fragment, Observer {
            when (it != null) {
                true -> {
                    broadcastFavoriteButton.setImageResource(R.drawable.ic_favorite_white_24dp)
                    broadcastFavoriteButton.tag = 1
                }
                false -> {
                    broadcastFavoriteButton.setImageResource(R.drawable.ic_favorite_border_white_24dp)
                    broadcastFavoriteButton.tag = 0
                }
            }
        })
    }

    private fun exportTracksToSpotify() {
        var hasExecuted = false
        var exportJob: Job? = null

        val loadingDialog =
            ExportLoadingDialog(requireContext()) {
                exportJob?.cancel()
            }.apply {
                display()
            }

        viewModel.tracksLiveData.observe(this, Observer { tracks ->
            tracks.forEach {
                sharedViewModel.fetchThirdPartyDataForTrack(it)
            }
        })

        // Launch export job when we have fetched all third-party data
        viewModel.showWithBroadcast.observe(this, Observer { (show, broadcast) ->
            viewModel.songsLiveData.observe(this, Observer { tracks ->
                if (!hasExecuted && tracks.all { t -> t.hasThirdPartyInfo }) {
                    exportJob = launch {
                        val uris = tracks.mapNotNull { t -> t.spotifyTrackUri }
                        val title = sharedViewModel.getBroadcastDownloadUiTitle(broadcast, show)

                        if (uris.isNotEmpty()) {
                            spotifyExportManager.exportTracksToDynamicPlaylistAsync(uris, title)
                                .await()
                                ?.let {
                                    sharedViewModel.openSpotify(requireContext(), it)
                                }

                            hasExecuted = true
                        }
                    }.also {
                        it.invokeOnCompletion {
                            loadingDialog.hide()
                            hasExecuted = true
                        }
                    }
                }
            })
        })
    }

    private fun exportTracksToYouTube() {
        var hasExecuted = false

        val loadingDialog =
            ExportLoadingDialog(requireContext()) {
                hasExecuted = true
            }.apply {
                display()
            }

        viewModel.tracksLiveData.observe(this, Observer { tracks ->
            tracks.forEach {
                sharedViewModel.fetchThirdPartyDataForTrack(it)
            }
        })

        viewModel.songsLiveData.observe(this, Observer { tracks ->
            if (!hasExecuted && tracks.all { t -> t.hasThirdPartyInfo }) {
                val ids = tracks.mapNotNull { t -> t.youTubeId }

                if (ids.isNotEmpty()) {
                    sharedViewModel.exportVideosToYouTubePlaylist(requireContext(), ids)
                    hasExecuted = true
                }

                loadingDialog.hide()
            }
        })
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
                loadScreen.hide()
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
            it.setImageDrawable(
                it.resources.getDrawable(
                    R.drawable.ic_delete_forever_white_24dp,
                    context?.theme
                )
            )
            it.tag = DELETE_ICON
            it.isEnabled = true
            it.alpha = 1f
        }
    }

    private fun enableDownloadIcon() {
        downloadDeleteIcon?.let {
            it.setImageDrawable(
                it.resources.getDrawable(
                    R.drawable.ic_file_download_white_24dp,
                    context?.theme
                )
            )
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

    companion object {
        const val DOWNLOAD_ICON = "download"
        const val DELETE_ICON = "delete"
    }
}
