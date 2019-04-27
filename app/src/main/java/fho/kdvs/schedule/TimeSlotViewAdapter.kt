package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellTimeslotBinding
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData

/** Adapter for a single timeslot card. */
class TimeSlotViewAdapter(onClick: (ClickData<TimeSlot>) -> Unit) :
    BindingRecyclerViewAdapter<TimeSlot, TimeSlotViewAdapter.ViewHolder>(onClick, TimeSlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellTimeslotBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(private val binding: CellTimeslotBinding) : BindingViewHolder<TimeSlot>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: TimeSlot) {
            binding.apply {
                clickListener = listener
                timeslot = item
            }
        }
    }

    fun onShowsChanged(shows: List<TimeSlot>) {
        submitList(shows)
    }
}