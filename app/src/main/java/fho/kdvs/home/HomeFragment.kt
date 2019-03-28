package fho.kdvs.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.ExoPlayer
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentHomeBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

class HomeFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private lateinit var viewModel: SharedViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@HomeFragment
            vm = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exoPlayerView.player = exoPlayer
    }

    /*
     * Consult #1:
     * https://proandroiddev.com/5-common-mistakes-when-using-architecture-components-403e9899f4cb
     */
    private fun subscribeToViewModel() {
        viewModel.currentShow.observe(this, Observer { show ->
            binding.currentShow = show
            binding.executePendingBindings()
            showName.isSelected = true
        })

        viewModel.nextShow.observe(this, Observer { show ->
            binding.nextShow = show
            binding.executePendingBindings()
        })

        viewModel.currentBroadcast.observe(this, Observer { broadcast ->

        })
    }
}

