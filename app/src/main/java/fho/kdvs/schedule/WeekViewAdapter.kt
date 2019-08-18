package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.R
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.schedule.TimeSlot.Companion.DUMMY_ID
import kotlinx.android.synthetic.main.cell_day_column.view.*
import kotlinx.android.synthetic.main.fragment_schedule.view.*
import timber.log.Timber


/** A [RecyclerView.Adapter] which cycles through days of the week */
class WeekViewAdapter(
    private val fragment: ScheduleFragment,
    private val days: List<ScheduleFragment.DayInfo>,
    private val kdvsPreferences: KdvsPreferences
) : RecyclerView.Adapter<WeekViewAdapter.ViewHolder>() {

    // Simple flag for scrolling to current show view. This will only be done once, after the fragment is created.
    private var scrollingToCurrentShow = true

    // Timeblock view, for synced scrolling.
    private var timeRecyclerView: RecyclerView? = null

    private var nestedScrollView: NestedScrollView? = null

    private var scrollY: Float? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val dayContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_day_column, parent, false) as ConstraintLayout
        timeRecyclerView = fragment.view?.timeRecyclerView
        return ViewHolder(dayContainer)
    }

    /** Return two more than number of items to enable looped scrolling. */
    override fun getItemCount(): Int = days.size + 2

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position % 7]

        val childAdapter = TimeSlotViewAdapter(kdvsPreferences.theme) { clickData ->
            // Here is where we navigate to the ShowDetailsFragment or display show selection view
            Timber.d("clicked ${clickData.item.names.joinToString()}")
            if (clickData.item.ids.count() > 1)
                fragment.showSelection(clickData.item)
            else
                fragment.viewModel.onClickTimeSlot(fragment.findNavController(), clickData.item)
        }

        val childLayoutManager = LinearLayoutManager(holder.recyclerView.context, RecyclerView.VERTICAL, false)

        // tag root constraint layout as the day name + position for debug purposes
        val parent = holder.recyclerView.parent as View
        parent.tag = day.dayName

        // configure each day
        holder.recyclerView.apply {
            adapter = childAdapter
            layoutManager = childLayoutManager
            setItemViewCacheSize(20)

            // TODO: timeslot dividers?
//            if (recyclerView.itemDecorationCount == 0){
//                val dividerItemDecoration = DividerItemDecoration(context.getDrawable(R.drawable.timeslot_divider)!!)
//                addItemDecoration(dividerItemDecoration)
//            }
        }

        day.timeSlotsLiveData.observe(fragment, Observer { timeslots ->
            val correctedTimeSlots = correctTimeSlotListWhenGap(timeslots)

            childAdapter.onShowsChanged(correctedTimeSlots)

            // Scroll to current show, only when the fragment is first created
            if (scrollingToCurrentShow) {
                val scheduleTime = TimeHelper.makeEpochRelativeTime(TimeHelper.getNow())
                if (scheduleTime.dayOfWeek.toString().capitalize() == day.dayName.capitalize()) {

                    // For whatever reason, the scrollTo position appears to be based off hour-block height (likely
                    // because we don't have access to the non-default timeSlot height yet upon time of binding)
                    val currentTimeSlot = timeslots.firstOrNull { t -> TimeHelper.isTimeSlotForCurrentShow(t) }
                    scrollY = (currentTimeSlot?.timeStart?.hour ?: 0) *
                            (fragment.context?.resources?.getDimension(R.dimen.timeslot_hour_height) ?: 0f)
                }
            }
        })

        holder.recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (nestedScrollView == null) initNestedScrollView(parent)

            if (scrollingToCurrentShow) {
                scrollY?.let {
                    nestedScrollView?.scrollTo(0, it.toInt())
                    scrollingToCurrentShow = false
                }
            } else {
                fragment.lastDayScrollY?.let {
                    nestedScrollView?.scrollTo(0, it)
                }
            }
        }
    }

    /**
     * As of Aug. 17 2019, there is a gap in the KDVS schedule grid as per the website... Friday 3:30-4:30PM
     * To correct for this we create a dummy timeslot in the position that the gap is in.
     * Otherwise, the gap will appear at the top of the day and make all of the day's timeslots offset.
     */
    private fun correctTimeSlotListWhenGap(timeslots: List<TimeSlot>): List<TimeSlot>{
        val correctedList = mutableListOf<TimeSlot>()
        correctedList.addAll(timeslots)

        var position: Int? = null
        var dummy: TimeSlot? = null

        for (i in 0..timeslots.size) {
            if ((i < timeslots.size - 1) && timeslots[i].timeEnd != timeslots[i+1].timeStart) {
                val isFirstHalfOrEntireSegment = timeslots[i].timeEnd?.dayOfWeek == timeslots[i+1].timeStart?.dayOfWeek

                position = i+1

                dummy = TimeSlot(
                    timeslots[i].timeEnd,
                    timeslots[i].timeStart,
                    isFirstHalfOrEntireSegment,
                    URLs.SHOW_IMAGE_PLACEHOLDER,
                    mutableListOf(DUMMY_ID),
                    mutableListOf("N/A")
                )
            }
        }

        dummy?.let {
            correctedList.add(position ?: 0, it)
        }

        return correctedList
    }

    private fun initNestedScrollView(parent: View) {
        nestedScrollView = parent.parent?.parent?.parent as NestedScrollView?
        nestedScrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, newY, _, _ ->
            fragment.lastDayScrollY = newY
        })
    }

    class ViewHolder(dayContainer: ConstraintLayout) : RecyclerView.ViewHolder(dayContainer) {
        val recyclerView: RecyclerView = dayContainer.dayRecyclerView
    }
}