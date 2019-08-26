package fho.kdvs.broadcast

import android.app.Activity
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
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
import fho.kdvs.R
import fho.kdvs.databinding.FragmentBroadcastDetailsBinding
import fho.kdvs.dialog.BinaryChoiceDialogFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.MainActivity
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.util.RequestCodes
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import kotlinx.android.synthetic.main.fragment_broadcast_details.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.File
import javax.inject.Inject

const val DOWNLOAD_ICON = 0
const val DELETE_ICON = 1

@kotlinx.serialization.UnstableDefault
class BroadcastDetailsFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: BroadcastDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(BroadcastDetailsViewModel::class.java)
            .also { it.initialize(showId, broadcastId) }

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(detailsRoot)

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
                    sharedViewModel.playPastBroadcast(broadcast, show, requireActivity())
                }
            }
        }

        downloadDeleteIcon.setOnClickListener { icon ->
            viewModel.broadcast.value?.let { broadcast ->
                viewModel.show.value?.let { show ->
                    val folder = sharedViewModel.getDownloadFolder()

                    folder?.let {
                        if (icon.tag == DOWNLOAD_ICON) {
                            downloadBroadcast(broadcast, show, folder)
                            setDownloadingIcon()
                        } else if (icon.tag == DELETE_ICON) {
                            displayDialog()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCodes.DOWNLOAD_DELETE_DIALOG -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.broadcast.value?.let { broadcast ->
                        viewModel.show.value?.let { show ->
                            deleteBroadcast(broadcast, show)
                            flipIcon()
                            Toast.makeText(requireContext(),  "Download deleted", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun subscribeToViewModel() {
        val fragment = this

        // TODO: mediator live data
        viewModel.broadcast.observe(fragment, Observer { broadcast ->
            Timber.d("Got broadcast: $broadcast")
            viewModel.show.observe(fragment, Observer { show ->
                if (sharedViewModel.isBroadcastDownloaded(broadcast, show)) {
                    setDownloadViewsVisible()

                    enableDeleteIcon()

                    LoadScreen.hideLoadScreen(detailsRoot)
                } else {
                    setPlaybackViewsAndHideProgressBar(broadcast)
                }
            })
        })

        viewModel.tracksWithFavorites.observe(fragment, Observer { (tracks, _) ->
            Timber.d("Got tracks: $tracks with liveFavorites")

            noTracksMessage.visibility = if (tracks.isEmpty()) View.VISIBLE
                else View.GONE

            tracksAdapter?.onTracksChanged(tracks)
        })
    }

    private fun downloadBroadcast(broadcast: BroadcastEntity, show: ShowEntity, folder: File) {
        if (kdvsPreferences.offlineMode == true) {
            sharedViewModel.makeOfflineModeToast(activity)
            return
        }

        val title = sharedViewModel.getBroadcastDownloadTitle(broadcast, show)
        val filename = sharedViewModel.getDownloadingFilename(title)
        val file = sharedViewModel.getDestinationFile(filename)

        (activity as? MainActivity)?.let { activity ->
            if (activity.isStoragePermissionGranted()) {
                val url = URLs.archiveForBroadcast(broadcast)

                url?.let {
                    if (!folder.exists())
                        folder.mkdirs()

                    try {
                        val request = sharedViewModel.makeDownloadRequest(url, title, file)

                        val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE)
                                as DownloadManager

                        downloadId = downloadManager.enqueue(request)

                        Toast.makeText(
                            activity as? MainActivity,
                            "Download started",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } catch (e: Exception) {
                        Timber.e("Error downloading broadcast: $e")
                        Toast.makeText(activity as? MainActivity, "Error downloading broadcast", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                Toast.makeText(activity as? MainActivity, "Download permission not granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun deleteBroadcast(broadcast: BroadcastEntity, show: ShowEntity) {
        val title = sharedViewModel.getBroadcastDownloadTitle(broadcast, show)
        val filename = sharedViewModel.getDownloadedFilename(title)
        val file = sharedViewModel.getDestinationFile(filename)

        downloadId?.let {
            val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE)
                    as DownloadManager
            downloadManager.remove(it)
        }

        sharedViewModel.deleteFile(file)
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
        }
    }

    private fun enableDownloadIcon() {
        downloadDeleteIcon?.let {
            it.setImageDrawable(it.resources.getDrawable(R.drawable.ic_arrow_downward_white_24dp, context?.theme))
            it.tag = DOWNLOAD_ICON
        }
    }

    private fun setDownloadViewsVisible() {
        archivePlayButton?.let { it.visibility = View.VISIBLE }
        downloadDeleteIcon?.let { it.visibility = View.VISIBLE }
    }

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
        dialog.setTargetFragment(this@BroadcastDetailsFragment, RequestCodes.SETTINGS_DIALOG)
        dialog.show(requireFragmentManager(), "tag")
    }
}