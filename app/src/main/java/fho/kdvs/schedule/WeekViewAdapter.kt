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

    private var currentDayPosition: Int? = null

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
            childAdapter.onShowsChanged(timeslots)

            // Scroll to current show, only when the fragment is first created
            if (scrollingToCurrentShow) {
                val scheduleTime = TimeHelper.makeEpochRelativeTime(TimeHelper.getNow())
                if (scheduleTime.dayOfWeek.toString().capitalize() == day.dayName.capitalize()) {
                    childLayoutManager.stackFromEnd = true

                    // For whatever reason, the scrollTo position appears to be based off hour-block height (likely
                    // because we don't have access to the non-default timeSlot height yet upon time of binding)
                    val currentTimeSlot = timeslots.first { t -> TimeHelper.isTimeSlotForCurrentShow(t) }
                    currentDayPosition = currentTimeSlot.timeStart?.hour
                }
            }
        })

        holder.recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (nestedScrollView == null) initNestedScrollView(parent)

            if (scrollingToCurrentShow) {
                currentDayPosition?.let {
                    scrollToDayPosition(parent, holder, it)
                    scrollingToCurrentShow = false
                }
            } else {
                fragment.lastDayScrollY?.let {
                    nestedScrollView?.scrollTo(0, it)
                }
            }
        }
    }

    private fun initNestedScrollView(parent: View) {
        nestedScrollView = parent.parent?.parent?.parent as NestedScrollView?
        nestedScrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, newY, _, _ ->
            fragment.lastDayScrollY = newY
        })
    }

    private fun scrollToDayPosition(parent: View, holder: ViewHolder, position: Int) {
        // TODO: Y position appears to be based off timeblocks and not the schedule...
        val parentRecycler = parent.parent as RecyclerView?
        val child = holder.recyclerView.getChildAt(9)
        val y = (child?.top?.toFloat() ?: 0.toFloat())
        nestedScrollView?.scrollTo(0, y.toInt())
    }

    class ViewHolder(dayContainer: ConstraintLayout) : RecyclerView.ViewHolder(dayContainer) {
        val recyclerView: RecyclerView = dayContainer.dayRecyclerView
    }
}