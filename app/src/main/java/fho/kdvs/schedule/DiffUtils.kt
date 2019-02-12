package fho.kdvs.schedule

import androidx.recyclerview.widget.DiffUtil

class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
    override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean =
        oldItem.ids == newItem.ids


    override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: TimeSlot, newItem: TimeSlot): Any? {
        return super.getChangePayload(oldItem, newItem) // TODO
    }
}