package fho.kdvs.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.show.ScheduleSelectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ScheduleSelectionFragment : DaggerFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: ScheduleSelectionViewModel

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main // TODO: cleaner way to do this? we have to suspend thread in order to get show order

    // Retrieves the timeslot from the arguments bundle. Throws an exception if it doesn't exist.
    private val timeslot: TimeSlot by lazy {
        arguments?.let { ScheduleSelectionFragmentArgs.fromBundle(it) }?.timeslot
            ?: throw IllegalArgumentException("Should have passed a timeslot to ScheduleSelectionFragment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleSelectionViewModel::class.java)
            .also { launch { it.initialize(timeslot) } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}