package fho.kdvs.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.CellShowBinding
import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.ui.BindingRecyclerViewAdapter
import fho.kdvs.ui.BindingViewHolder
import fho.kdvs.util.ShowDiffCallback
import fho.kdvs.viewmodel.KdvsViewModelFactory
import fho.kdvs.viewmodel.ScheduleViewModel
import kotlinx.android.synthetic.main.cell_day_column.view.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ScheduleFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: ScheduleViewModel

    /** Outer [RecyclerView] that will hold RecyclerViews for each day. */
    private lateinit var weekRecyclerView: RecyclerView
    private lateinit var weekLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleViewModel::class.java)
            .also {
                it.fetchShows()
            }
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

        val snapHelper = PagerSnapHelper()

        val weekData = Day.values().map { day -> DayInfo(day, Quarter.WINTER, 2019) }

        weekRecyclerView.apply {
            adapter = WeekViewAdapter(weekData)
            setHasFixedSize(true)
            snapHelper.attachToRecyclerView(this)
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

        // start week view on current day
        // TODO or from saved instance state
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        weekRecyclerView.layoutManager?.scrollToPosition(today)
    }

    inner class DayInfo(day: Day, quarter: Quarter, year: Int) {
        val dayName = day.name
        val showsLiveData: LiveData<List<ShowEntity>> = viewModel.getShowsForDay(day, quarter, year)
    }

    inner class WeekViewAdapter(private val days: List<ScheduleFragment.DayInfo>) :
        RecyclerView.Adapter<WeekViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
            val dayContainer = LayoutInflater.from(parent.context)
                .inflate(R.layout.cell_day_column, parent, false) as ConstraintLayout
            return WeekViewHolder(dayContainer)
        }

        /** Return two more than number of items to enable looped scrolling */
        override fun getItemCount(): Int = days.size + 2

        override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
            val day = days[position % 7]

            val childAdapter = ShowViewAdapter().apply {
                clickHandler = {
                    Timber.d("clicked ${it.item.name}")
                }
            }

            // tag root constraint layout as the day name + position for debug purposes
            val parent = holder.recyclerView.parent as View
            parent.tag = "${day.dayName}_$position"

            // configure each day
            holder.textView.text = day.dayName
            holder.recyclerView.apply {
                layoutManager = LinearLayoutManager(holder.recyclerView.context, RecyclerView.VERTICAL, false)
                adapter = childAdapter
            }

            day.showsLiveData.observe(this@ScheduleFragment, Observer { shows ->
                childAdapter.onShowsChanged(shows)
            })
        }
    }

    class WeekViewHolder(dayContainer: ConstraintLayout) : RecyclerView.ViewHolder(dayContainer) {
        val textView: TextView = dayContainer.textView
        val recyclerView: RecyclerView = dayContainer.recyclerView
    }
}

class ShowViewAdapter :
    BindingRecyclerViewAdapter<ShowEntity, ShowViewAdapter.ViewHolder>(ShowDiffCallback()) {

    class ViewHolder(private val binding: CellShowBinding) : BindingViewHolder<ShowEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowEntity) {
            Glide.with(binding.root)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .error(R.drawable.show_placeholder)
                        .apply(RequestOptions.centerCropTransform())
                )
                .load(item.defaultImageHref)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.showImage)

            binding.apply {
                clickListener = listener
                show = item
            }
        }
    }

    fun onShowsChanged(shows: List<ShowEntity>) {
        submitList(shows)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellShowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }
}
