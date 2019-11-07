package fho.kdvs.topmusic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentTopmusicDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.global.ui.LoadScreen
import kotlinx.android.synthetic.main.fragment_topmusic_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TopMusicDetailsFragment : DaggerFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: TopMusicDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var fragmentTopMusicDetailsBinding: FragmentTopmusicDetailsBinding

    private lateinit var topMusicItems: List<TopMusicEntity>

    private var topMusicViewAdapter: TopMusicViewAdapter? = null

    private var topMusicLayoutManager: LinearLayoutManager? = null

    private val snapHelper = PagerSnapHelper()

    // Simple flag for scrolling to clicked-on item view. This will only be done once, after the fragment is created.
    private var scrollingToCurrentItem = true

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private val topMusic: TopMusicEntity by lazy {
        arguments?.let { TopMusicDetailsFragmentArgs.fromBundle(it) }?.topMusic
            ?: throw IllegalArgumentException("Should have passed a TopMusicEntity to TopMusicDetailsFragment")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(TopMusicDetailsViewModel::class.java)
            .also {
                it.initialize(topMusic.weekOf, topMusic.type!!)
            }

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentTopMusicDetailsBinding = FragmentTopmusicDetailsBinding.inflate(inflater, container, false)

        fragmentTopMusicDetailsBinding.apply {
            topMusicData = topMusic
            sharedVm = sharedViewModel
        }

        return fragmentTopMusicDetailsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(topMusicDetailsRoot)

        topMusicViewAdapter = TopMusicViewAdapter { }
        topMusicLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        topMusicRecyclerView?.run {
            adapter = topMusicViewAdapter
            layoutManager = topMusicLayoutManager
            setHasFixedSize(true)

            onFlingListener = null
            clearOnScrollListeners()
            snapHelper.attachToRecyclerView(this)
        }

        topMusicRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = getCurrentItem()
                    onItemChanged(position)
                }
            }
        })
    }

    private fun subscribeToViewModel() {
        viewModel.liveTopMusic.observe(this, Observer { items ->
            Timber.d("Got updated topMusic")

            topMusicItems = items
            topMusicViewAdapter?.onTopMusicChanged(items)

            if (scrollingToCurrentItem) {
                topMusicRecyclerView?.scrollToPosition(topMusic.position ?: 0)
                scrollingToCurrentItem = false
            }

            LoadScreen.hideLoadScreen(topMusicDetailsRoot)
        })
    }

    private fun getCurrentItem(): Int {
        return (topMusicRecyclerView.layoutManager as LinearLayoutManager)
            .findFirstVisibleItemPosition()
    }

    private fun onItemChanged(position: Int) {
        if (::topMusicItems.isInitialized) {
            val item = topMusicItems.getOrNull(position)
            item?.let {topMusic ->

                album.text = topMusic.album ?: ""
                artist.text = topMusic.artist ?: ""

                if (topMusic.year != null || topMusic.label != null) {
                    when {
                        topMusic.label == null -> albumInfo.text = topMusic.year.toString()
                        topMusic.year == null -> albumInfo.text = topMusic.label
                        else -> albumInfo.text = albumInfo.resources.getString(R.string.album_info,
                            topMusic.year, topMusic.label)
                    }

                    albumInfo.visibility = View.VISIBLE
                } else albumInfo.visibility = View.GONE

                spotifyIcon.visibility = if (topMusic.spotifyAlbumUri?.isNotBlank() == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }
}
