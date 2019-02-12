package fho.kdvs.show

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentShowDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import timber.log.Timber
import javax.inject.Inject

class ShowDetailsFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: ShowDetailsViewModel

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Timber.d("Initializing")
        val binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
            .apply { vm = viewModel }
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        viewModel.broadcastsLiveData.observe(this, Observer {
            Timber.d("got broadcasts: $it")
            configureBroadcastList()
        })
    }

    private fun configureBroadcastList() {

    }
}