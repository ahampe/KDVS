package fho.kdvs.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.CellShowSelectionBinding
import fho.kdvs.databinding.FragmentScheduleSelectionBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.show.ScheduleSelectionViewModel
import kotlinx.android.synthetic.main.fragment_schedule_selection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ScheduleSelectionFragment : DaggerFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: ScheduleSelectionViewModel

    private var showSelectionViewAdapter: ShowSelectionViewAdapter? = null

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    // Retrieves the timeslot from the arguments bundle. Throws an exception if it doesn't exist.
    private val timeslot: TimeSlot by lazy {
        arguments?.let { ScheduleSelectionFragmentArgs.fromBundle(it) }?.timeslot
            ?: throw IllegalArgumentException("Should have passed a TimeSlot to ScheduleSelectionFragment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleSelectionViewModel::class.java)
            .also {
                it.initialize(timeslot)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentScheduleSelectionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showSelectionViewAdapter = ShowSelectionViewAdapter {
            Timber.d("Clicked ${it.item.second}")
            viewModel.onClickShowSelection(findNavController(), it.item.first)
        }

        showSelectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = showSelectionViewAdapter

            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.show_selection_divider, context.theme))
            addItemDecoration(dividerItemDecoration)
        }

        showSelectionViewAdapter?.submitList(viewModel.pairedIdsAndNames)
    }
}