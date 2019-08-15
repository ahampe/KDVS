package fho.kdvs.show

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
import fho.kdvs.R
import fho.kdvs.databinding.FragmentShowDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import kotlinx.android.synthetic.main.fragment_show_details.*
import timber.log.Timber
import javax.inject.Inject

class ShowDetailsFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: ShowDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private var broadcastListAdapter: ShowBroadcastsAdapter? = null

    // Retrieves the show ID from the arguments bundle. Throws an exception if it doesn't exist.
    private val showId: Int by lazy {
        arguments?.let { ShowDetailsFragmentArgs.fromBundle(it) }?.showId
            ?: throw IllegalArgumentException("Should have passed a showId to ShowDetailsFragment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ShowDetailsViewModel::class.java)
            .also { it.initialize(showId) }

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedViewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)

        val binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
            .apply { vm = viewModel }
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        broadcastListAdapter = ShowBroadcastsAdapter {
            Timber.d("clicked ${it.item}")
            viewModel.onClickBroadcast(findNavController(), it.item)
        }

        broadcastRecycler.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = broadcastListAdapter
        }
    }

    private fun subscribeToViewModel() {
        viewModel.broadcastsLiveData.observe(this, Observer { broadcasts ->
            Timber.d("got broadcasts: $broadcasts")
            broadcastListAdapter?.onBroadcastsChanged(broadcasts)
        })

        viewModel.show.observe(this, Observer { show ->
            star.setOnClickListener { sharedViewModel.onClickStar(star, show, context) }
        })

        viewModel.subscription.observe(this, Observer {
            when(it != null) {
                true -> {
                    star.setImageResource(R.drawable.ic_star_white_24dp)
                    star.tag = 1
                }
                false -> {
                    star.setImageResource(R.drawable.ic_star_border_white_24dp)
                    star.tag = 0
                }
            }
        })
    }
}