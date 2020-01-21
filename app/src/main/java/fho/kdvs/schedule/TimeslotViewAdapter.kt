package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellTimeslotBinding
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData

/** Adapter for a single timeslot card on the [ScheduleFragment]. */
class TimeslotViewAdapter(
    private val selectedTheme: Int?,
    onClick: (ClickData<ScheduleTimeslot>) -> Unit
) : BindingRecyclerViewAdapter<ScheduleTimeslot, TimeslotViewAdapter.ViewHolder>(
    onClick,
    TimeSlotDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellTimeslotBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, selectedTheme)
    }

    class ViewHolder(
        private val binding: CellTimeslotBinding,
        private val selectedTheme: Int?
    ) : BindingViewHolder<ScheduleTimeslot>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ScheduleTimeslot) {
            binding.apply {
                clickListener = listener
                timeslot = item
                theme = selectedTheme ?: 0
            }
        }
    }

    fun onShowsChanged(shows: List<ScheduleTimeslot>) {
        submitList(shows)
    }
}