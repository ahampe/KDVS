package fho.kdvs.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.fragment_schedule.*
import kotlinx.android.synthetic.main.fragment_schedule.view.*
import timber.log.Timber
import javax.inject.Inject

class ScheduleFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    lateinit var viewModel: ScheduleViewModel
    lateinit var sharedViewModel: SharedViewModel

    // Outer horizontal RecyclerView. Holds a vertical RecyclerView for each day of week.
    private var weekLayoutManager: LinearLayoutManager? = null

    // Vertical RecyclerView. Holds 24 hour time cells.
    private var timeGridLayoutManager: LinearLayoutManager? = null

    // Simple flag for scrolling to today's date. This will only be done once, after the fragment is created.
    private var scrollingToToday = true

    // Flags for retaining schedule scroll state upon re-entry to fragment
    var lastWeekPosition: Int? = null
    var lastDayScrollY: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleViewModel::class.java)
            .also { it.fetchShows() }

        sharedViewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        subscribeToViewModel()
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(scheduleRoot)

        // Configure the layout manager and keep a reference to it
        weekLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            .also { weekRecyclerView.layoutManager = it }

        timeGridLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            .also { timeRecyclerView.layoutManager = it}

        // Scroll to today, only when the fragment is first created
        if (scrollingToToday) {
            val position = TimeHelper.getLocalNow().dayOfWeek.value
            weekLayoutManager?.scrollToPosition(position)
            setDayAbbreviationsWithSelectedPos(position)
            scrollingToToday = false
        } else {
            lastWeekPosition?.let {
                // TODO: For some reason the view scrolls back to Sunday after this is called -- likely due to how layout is restored
                weekLayoutManager?.scrollToPosition(it)
                setDayAbbreviationsWithSelectedPos(it)
            }
        }

        LoadScreen.hideLoadScreen(scheduleRoot)
    }

    /** Reconfigures the week recycler view and time recycler view. Use when the quarter-year changes or the fragment is recreated. */
    private fun configureViews() {
        // Bail early if there isn't a quarter ready yet
        val (savedQuarter, savedYear) = sharedViewModel.loadQuarterYear() ?: return

        // Create data for each day
        val weekData = Day.values().map { day -> DayInfo(day, savedQuarter, savedYear) }

        val snapHelper = PagerSnapHelper()

        search?.run {
            setOnClickListener { viewModel.onClickSearch(findNavController()) }
        }

        timeRecyclerView?.run {
            adapter = TimeGridViewAdapter(this@ScheduleFragment)
            setHasFixedSize(true)
            layoutManager = timeGridLayoutManager
            isNestedScrollingEnabled = false
        }

        weekRecyclerView?.run {
            adapter = WeekViewAdapter(this@ScheduleFragment, weekData, kdvsPreferences)
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

                lastWeekPosition = weekLayoutManager?.findFirstCompletelyVisibleItemPosition()

                // TODO: skips frames on Sunday -> Monday and Monday -> Sunday

                val firstVisiblePos = weekLayoutManager?.findFirstVisibleItemPosition()
                if (firstVisiblePos == 8) {
                    weekLayoutManager?.scrollToPosition(1)
                    setDayAbbreviationsWithSelectedPos(1)
                }

                val firstCompletelyVisiblePos = weekLayoutManager?.findFirstCompletelyVisibleItemPosition()
                if (firstCompletelyVisiblePos == 0) {
                    weekLayoutManager?.scrollToPosition(7)
                    setDayAbbreviationsWithSelectedPos(7)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val newView = snapHelper.findSnapView(weekLayoutManager) as ConstraintLayout

                    Timber.d("scrolled to ${newView.tag}")

                    val position = Day.valueOf(newView.tag.toString()).ordinal
                    setDayAbbreviationsWithSelectedPos(position)
                }
            }
        })
    }

    private fun setDayAbbreviationsWithSelectedPos(pos: Int) {
        val abbreviationViews = listOf<TextView>(
            dayAbbreviations.sun,
            dayAbbreviations.mon,
            dayAbbreviations.tues,
            dayAbbreviations.wed,
            dayAbbreviations.thurs,
            dayAbbreviations.fri,
            dayAbbreviations.sat
        )

        abbreviationViews.forEachIndexed { i, v ->
            if (i == pos % 7)
                v.setTextColor(ResourcesCompat.getColor(dayAbbreviations.resources,
                    R.color.colorWhite,
                    dayAbbreviations.context.theme))
            else
                v.setTextColor(ResourcesCompat.getColor(dayAbbreviations.resources,
                    R.color.colorPrimary,
                    dayAbbreviations.context.theme))
        }
    }

    fun showSelection(timeslot: TimeSlot) {
        fragmentManager?.let {
            val args = Bundle()
            args.putParcelable("timeslot", timeslot)

            val newFragment = ScheduleSelectionFragment()

            newFragment.arguments = args
            newFragment.show(it, "schedule_selection_fragment")
        }
    }

    /** This is where any [LiveData] in the ViewModel should be hooked up to [Observer]s. */
    private fun subscribeToViewModel() {
        sharedViewModel.run {
            allQuarterYearsLiveData.observe(viewLifecycleOwner, Observer {
                configureViews()

                // When new quarter-years happen (which should only happen when a new quarter starts), update the spinner
                // and cancel nonrecurring subscriptions
                it.first().let { q ->
                    if (q != kdvsPreferences.mostRecentQuarterYear) {
                        if (!kdvsPreferences.isInitialLaunch())
                            this.onNewQuarter(context)
                        kdvsPreferences.mostRecentQuarterYear = q
                    }
                }
            })
        }
    }

    /** This class will hold all the data that the [WeekViewAdapter] needs. */
    inner class DayInfo(day: Day, quarter: Quarter, year: Int) {
        val dayName = day.name
        val timeSlotsLiveData: LiveData<List<TimeSlot>> = viewModel.getShowsForDay(day, quarter, year)
    }
}
