package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellTimeslotBinding
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData

/** Adapter for a single timeslot card. */
class TimeSlotViewAdapter(
    private val selectedTheme: Int?,
    onClick: (ClickData<TimeSlot>) -> Unit
) : BindingRecyclerViewAdapter<TimeSlot, TimeSlotViewAdapter.ViewHolder>(onClick, TimeSlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellTimeslotBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, selectedTheme)
    }

    class ViewHolder(
        private val binding: CellTimeslotBinding,
        private val selectedTheme: Int?
    ) : BindingViewHolder<TimeSlot>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: TimeSlot) {
            binding.apply {
                clickListener = listener
                timeslot = item
                theme = selectedTheme ?: 0
            }
        }
    }

    fun onShowsChanged(shows: List<TimeSlot>) {
        submitList(shows)
    }
}