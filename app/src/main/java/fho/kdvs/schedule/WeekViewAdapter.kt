package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.R
import fho.kdvs.global.extensions.MyDividerItemDecoration
import kotlinx.android.synthetic.main.cell_day_column.view.*
import timber.log.Timber

/** A [RecyclerView.Adapter] which cycles through days of the week */
class WeekViewAdapter(
    private val fragment: ScheduleFragment,
    private val days: List<ScheduleFragment.DayInfo>
) : RecyclerView.Adapter<WeekViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val dayContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_day_column, parent, false) as ConstraintLayout
        return ViewHolder(dayContainer)
    }

    /** Return two more than number of items to enable looped scrolling. */
    override fun getItemCount(): Int = days.size + 2

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position % 7]

        val childAdapter = TimeSlotViewAdapter { clickData ->
            // Here is where we navigate to the ShowDetailsFragment
            Timber.d("clicked ${clickData.item.names.joinToString()}")
            fragment.viewModel.onClickTimeSlot(fragment.findNavController(), clickData.item)
        }

        val childLayoutManager = LinearLayoutManager(holder.recyclerView.context, RecyclerView.VERTICAL, false)

        // tag root constraint layout as the day name + position for debug purposes
        val parent = holder.recyclerView.parent as View
        parent.tag = "${day.dayName}_$position"

        // configure each day
        holder.textView.text = day.dayName
        holder.recyclerView.apply {
            adapter = childAdapter
            layoutManager = childLayoutManager

            val dividerItemDecoration = MyDividerItemDecoration(context.getDrawable(R.drawable.timeslot_divider))
            addItemDecoration(dividerItemDecoration)
        }

        day.timeSlotsLiveData.observe(fragment, Observer { timeslots ->
            childAdapter.onShowsChanged(timeslots)
        })
    }

    class ViewHolder(dayContainer: ConstraintLayout) : RecyclerView.ViewHolder(dayContainer) {
        val textView: TextView = dayContainer.textView
        val recyclerView: RecyclerView = dayContainer.recyclerView
    }
}