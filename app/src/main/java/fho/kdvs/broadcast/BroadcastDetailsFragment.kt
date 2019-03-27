package fho.kdvs.broadcast

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentBroadcastDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.fragment_broadcast_details.*
import timber.log.Timber
import javax.inject.Inject

class BroadcastDetailsFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: BroadcastDetailsViewModel

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

        tracksAdapter = BroadcastTracksAdapter {
            Timber.d("Clicked ${it.item}")
        }

        trackRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = tracksAdapter
        }
    }

    private fun subscribeToViewModel() {
        viewModel.tracks.observe(this, Observer { tracks ->
            Timber.d("Got tracks: $tracks")
            tracksAdapter?.onTracksChanged(tracks)
        })
    }
}