package fho.kdvs.broadcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentBroadcastDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import kotlinx.android.synthetic.main.fragment_broadcast_details.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(BroadcastDetailsViewModel::class.java)
            .also { it.initialize(showId, broadcastId) }

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
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

        tracksAdapter = BroadcastTracksAdapter(viewModel, sharedViewModel) {
            Timber.d("Clicked ${it.item}")
            viewModel.onClickTrack(this.findNavController(), it.item)
        }

        trackRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = tracksAdapter
        }
    }

    private fun subscribeToViewModel() {
        val fragment = this
        viewModel.broadcast.observe(fragment, Observer { broadcast ->
            Timber.d("Got broadcast: $broadcast")
            setPlayButton(broadcast)
        })

        viewModel.tracks.observe(fragment, Observer { tracks ->
            Timber.d("Got tracks: $tracks")

            if (tracks.isEmpty())
                noTracksMessage.visibility = View.VISIBLE
            else
                noTracksMessage.visibility = View.GONE

            tracksAdapter?.onTracksChanged(tracks)

            viewModel.getFavoritesForTracks(tracks)

            viewModel.favorites.forEach {
                it.observe(fragment, Observer { favorite ->
                    if (favorite != null)
                        viewModel.favoritedTracks.add(favorite.trackId)
                })
            }
        })
    }

    private fun setPlayButton(broadcast: BroadcastEntity) {
        if (HttpHelper.isConnectionAvailable(URLs.playlistForBroadcast(broadcast)))
            archive_playButton.visibility = View.VISIBLE
    }
}