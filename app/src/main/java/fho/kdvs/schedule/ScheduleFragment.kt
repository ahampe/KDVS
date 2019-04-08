package fho.kdvs.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var weekLayoutManager: LinearLayoutManager? = null

    // Simple flag for scrolling to today's date. This will only be done once, after the fragment is created.
    private var scrollingToToday = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleViewModel::class.java)
            .also { it.fetchShows() }

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure the layout manager and keep a reference to it
        weekLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            .also { weekRecyclerView.layoutManager = it }

        // Scroll to today, only when the fragment is first created
        // TODO this could be done with a custom layout manager, without the ugly boolean
        if (scrollingToToday) {
            weekLayoutManager?.scrollToPosition(LocalDate.now().dayOfWeek.value)
            scrollingToToday = false
        }
    }

    /** Reconfigures the week recycler view. Use when the quarter-year changes or the fragment is recreated. */
    private fun configureWeekView() {
        // Bail early if there isn't a quarter ready yet
        val (savedQuarter, savedYear) = viewModel.loadQuarterYear() ?: return

        // Create data for each day
        val weekData = Day.values().map { day -> DayInfo(day, savedQuarter, savedYear) }

        val snapHelper = PagerSnapHelper()

        weekRecyclerView?.run {
            adapter = WeekViewAdapter(this@ScheduleFragment, weekData)
            setHasFixedSize(true)

            // explicitly clear the onFling and onScroll listeners; the new snapHelper will replace them
            onFlingListener = null
            clearOnScrollListeners()
            snapHelper.attachToRecyclerView(this)
        }

        weekRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /** Allows looped scrolling */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisiblePos = weekLayoutManager?.findFirstVisibleItemPosition()
                if (firstVisiblePos == 8) {
                    weekLayoutManager?.scrollToPosition(1)
                }

                val firstCompletelyVisiblePos = weekLayoutManager?.findFirstCompletelyVisibleItemPosition()
                if (firstCompletelyVisiblePos == 0) {
                    weekLayoutManager?.scrollToPosition(7)
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

    /** This is where any [LiveData] in the ViewModel should be hooked up to [Observer]s. */
    private fun subscribeToViewModel() {
        val fragment = this

        viewModel.run {
            // When a new quarter-year is selected, redraw the week recycler:
            selectedQuarterYearLiveData.observe(fragment, Observer {
                configureWeekView()
            })

            // When new quarter-years happen (which should only happen when a new quarter starts), update the spinner
            allQuarterYearsLiveData.observe(fragment, Observer {
                configureWeekView()
            })
        }
    }

    /** This class will hold all the data that the [WeekViewAdapter] needs. */
    inner class DayInfo(day: Day, quarter: Quarter, year: Int) {
        val dayName = day.name
        val timeSlotsLiveData: LiveData<List<TimeSlot>> = viewModel.getShowsForDay(day, quarter, year)
    }
}
