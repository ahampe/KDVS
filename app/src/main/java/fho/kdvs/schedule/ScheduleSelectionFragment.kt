package fho.kdvs.schedule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.multibindings.IntoMap
import fho.kdvs.R
import fho.kdvs.databinding.FragmentScheduleSelectionBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.injection.ViewModelKey
import fho.kdvs.show.ScheduleSelectionViewModel
import kotlinx.android.synthetic.main.fragment_schedule_selection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@Module
abstract class UiModule {

    @Binds
    abstract fun bindViewModelFactory(factory: KdvsViewModelFactory): ViewModelProvider.Factory

    @PerFragment
    @ContributesAndroidInjector(modules = [(ScheduleSelectionModule::class)])
    abstract fun contributeFilterFragment(): ScheduleSelectionFragment
}

@Module
abstract class ScheduleSelectionModule {

    @Binds
    @IntoMap
    @PerFragment
    @ViewModelKey(ScheduleSelectionViewModel::class)
    abstract fun bindViewModel(viewModel: ScheduleSelectionModule): ViewModel
}

class ScheduleSelectionFragment : BottomSheetDialogFragment(), CoroutineScope {
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

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleSelectionViewModel::class.java)
            .also {
                it.initialize(timeslot)
            }

        val scheduleSelectionFragment = ScheduleSelectionFragment.newInstance()
        scheduleSelectionFragment.show(fragmentManager, "schedule_selection_fragment")
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

    companion object {
        @JvmStatic
        fun newInstance() : ScheduleSelectionFragment  {
            return ScheduleSelectionFragment()
        }
    }
}