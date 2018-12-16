package fho.kdvs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import fho.kdvs.KdvsViewModel
import fho.kdvs.R
import fho.kdvs.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var viewModel: KdvsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity()).get(KdvsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_home, container, false)
        val view = binding.root

        binding.setLifecycleOwner(requireActivity())
        binding.viewModel = viewModel

        return view
    }

    
}
