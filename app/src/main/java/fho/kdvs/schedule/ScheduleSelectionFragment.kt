package fho.kdvs.schedule

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
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
import fho.kdvs.global.PerFragment
import fho.kdvs.injection.ViewModelKey
import kotlinx.android.synthetic.main.fragment_schedule_selection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@Module
abstract class SelectionUiModule {

    @Binds
    abstract fun bindViewModelFactory(vmFactory: KdvsViewModelFactory): ViewModelProvider.Factory

    @PerFragment
    @ContributesAndroidInjector(modules = [(ScheduleSelectionModule::class)])
    abstract fun contributeScheduleSelectionFragment(): ScheduleSelectionFragment
}

@Module
abstract class ScheduleSelectionModule: ViewModel() {

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

    // Retrieves the timeStart string from the arguments bundle. Throws an exception if it doesn't exist.
    private val timeStart: String by lazy {
        arguments?.let { ScheduleSelectionFragmentArgs.fromBundle(it) }?.timeStart
            ?: throw IllegalArgumentException("Should have passed a string to ScheduleSelectionFragment")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val root = LinearLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        root.setPadding(0,0,0,0)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleSelectionViewModel::class.java)
            .also {
                it.initialize()
            }

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
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
            this.dismiss()
            viewModel.onClickShowSelection(findNavController(), it.item.first)
        }

        showSelectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = showSelectionViewAdapter

            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.show_selection_divider, context.theme))
            addItemDecoration(dividerItemDecoration)
        }

        activity?.runOnUiThread {
            val time = OffsetDateTime.parse(timeStart)

            launch {
                val shows = viewModel.allOrderedShowsForTime(time)
                if (shows.isNotEmpty()) {
                    val pairs = viewModel.getPairedIdsAndNamesForShows(shows)
                    activity?.runOnUiThread {
                        showSelectionViewAdapter?.submitList(pairs)
                    }
                }
            }
        }
    }
}