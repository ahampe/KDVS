package fho.kdvs.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import kotlinx.android.synthetic.main.fragment_schedule.*
import org.threeten.bp.LocalDate
import timber.log.Timber
import javax.inject.Inject

class ScheduleFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    lateinit var viewModel: ScheduleViewModel

    // Outer horizontal RecyclerView. Holds a vertical RecyclerView for each day of week.
    private lateinit var weekRecyclerView: RecyclerView
    private lateinit var weekLayoutManager: LinearLayoutManager

    // Simple flag for scrolling to today's date. This will only be done once, after the fragment is created.
    private var scrollingToToday = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleViewModel::class.java)
            .also { it.fetchShows() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        weekRecyclerView = view.findViewById(R.id.weekRecyclerView)
        weekLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        weekRecyclerView.layoutManager = weekLayoutManager

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToViewModel()

        // Listen for changes to the quarter-year
        quarterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newQuarterYear = parent?.adapter?.getItem(position) as? QuarterYear ?: return
                Timber.d("Selected $newQuarterYear")
                viewModel.selectQuarterYear(newQuarterYear)
            }
        }

        // Scroll to today, only when the fragment is first created
        if (scrollingToToday) {
            weekLayoutManager.scrollToPosition(LocalDate.now().dayOfWeek.value)
            scrollingToToday = false
        }

        // TODO doesn't work... need to scroll when the adapter is ready
        // Scroll to the same approximate location in the inner recycler
//        val dayStartPos = savedInstanceState?.optInt(DAY_SCROLL_POS)
//        dayStartPos?.let {
//            val todayViewHolder = weekRecyclerView.findViewHolderForAdapterPosition(weekStartPos) as? ViewHolder
//            val todayLayoutManager = todayViewHolder?.recyclerView?.layoutManager as? LinearLayoutManager
//
//            todayLayoutManager?.scrollToPosition(dayStartPos)
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // get position within Week Recycler
        val posInWeek = weekLayoutManager.findFirstVisibleItemPosition()

        // save position within Day Recycler TODO get this to work
        val currentViewHolder =
            weekRecyclerView.findViewHolderForAdapterPosition(posInWeek) as? WeekViewAdapter.ViewHolder
        val currentLayoutManager = currentViewHolder?.recyclerView?.layoutManager as? LinearLayoutManager
        val posInDay = currentLayoutManager?.findFirstVisibleItemPosition()
        if (posInDay != null && posInDay != RecyclerView.NO_POSITION) {
            outState.putInt(DAY_SCROLL_POS, posInDay)
        }

        super.onSaveInstanceState(outState)
    }

    /** Reconfigures the week recycler view. Use when the quarter-year changes or the fragment is recreated. */
    private fun configureWeekView() {
        // Bail early if there isn't a quarter ready yet
        val (savedQuarter, savedYear) = viewModel.loadQuarterYear() ?: return

        // Create data for each day
        val weekData = Day.values().map { day -> DayInfo(day, savedQuarter, savedYear) }

        val snapHelper = PagerSnapHelper()

        weekRecyclerView.run {
            adapter = WeekViewAdapter(this@ScheduleFragment, weekData)
            setHasFixedSize(true)

            // explicitly clear the onFling listener; the new snapHelper will replace it
            onFlingListener = null
            snapHelper.attachToRecyclerView(this)

            clearOnScrollListeners()
        }

        weekRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /** Allows looped scrolling */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisiblePos = weekLayoutManager.findFirstVisibleItemPosition()
                if (firstVisiblePos == 8) {
                    weekLayoutManager.scrollToPosition(1)
                }

                val firstCompletelyVisiblePos = weekLayoutManager.findFirstCompletelyVisibleItemPosition()
                if (firstCompletelyVisiblePos == 0) {
                    weekLayoutManager.scrollToPosition(7)
                }
            }

            /** For debug purposes */
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val newView = snapHelper.findSnapView(weekLayoutManager) as ConstraintLayout
                    Timber.d("scrolled to ${newView.tag}")
                }
            }
        })
    }

    /** Reconfigures the quarter picker. Automatically invoked when the list of quarters changes. */
    private fun configureQuarterSpinner(quarterYears: List<QuarterYear>) {
        // No use showing the spinner if there's 0 or 1 quarters
        if (quarterYears.size <= 1) {
            quarterSpinner.visibility = View.GONE
            return
        } else {
            quarterSpinner.visibility = View.VISIBLE
        }

        quarterSpinner.adapter = ArrayAdapter<QuarterYear>(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, quarterYears
        )
    }

    /** This is where any [LiveData] in the ViewModel should be hooked up to [Observer]s. */
    private fun subscribeToViewModel() {
        val fragment = this

        viewModel.run {
            // When a new quarter-year is selected, redraw the week recycler:
            selectedQuarterYearLiveData.observe(fragment, Observer {
                configureWeekView()
            })

            // When new quarter-years happen (which should only happen when a new quarter starts), update the spinner
            allQuarterYearsLiveData.observe(fragment, Observer { quarterYears ->
                configureQuarterSpinner(quarterYears)
                configureWeekView()
            })
        }
    }

    /** This class will hold all the data that the [WeekViewAdapter] needs. */
    inner class DayInfo(day: Day, quarter: Quarter, year: Int) {
        val dayName = day.name
        val timeSlotsLiveData: LiveData<List<TimeSlot>> = viewModel.getShowsForDay(day, quarter, year)
    }

    companion object {
        // saved instance state keys:
        const val WEEK_SCROLL_POS = "WEEK_SCROLL_POS"
        const val DAY_SCROLL_POS = "DAY_SCROLL_POS"
    }
}
