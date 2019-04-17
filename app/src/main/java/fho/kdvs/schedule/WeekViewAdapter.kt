package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import fho.kdvs.R
import fho.kdvs.global.extensions.DividerItemDecoration
import fho.kdvs.global.extensions.MyPreloadModelProvider
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
        holder.recyclerView.apply {
            adapter = childAdapter
            layoutManager = childLayoutManager
            setItemViewCacheSize(10)

            if (recyclerView.itemDecorationCount == 0){
                val dividerItemDecoration = DividerItemDecoration(context.getDrawable(R.drawable.timeslot_divider))
                addItemDecoration(dividerItemDecoration)
            }
        }

        day.timeSlotsLiveData.observe(fragment, Observer { timeslots ->
            childAdapter.onShowsChanged(timeslots)

//            val urls = timeslots.map { t -> t.imageHref ?: "" }.toList()
//            val sizeProvider = ViewPreloadSizeProvider<String>(holder.recyclerView)
//            val modelProvider = MyPreloadModelProvider(urls, fragment)
//            val preloader = RecyclerViewPreloader<TimeSlot>(Glide.with(fragment), modelProvider, sizeProvider, 10)
        })
    }

    class ViewHolder(dayContainer: ConstraintLayout) : RecyclerView.ViewHolder(dayContainer) {
        val recyclerView: RecyclerView = dayContainer.recyclerView
    }
}