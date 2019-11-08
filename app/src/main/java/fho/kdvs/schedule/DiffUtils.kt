package fho.kdvs.schedule

import androidx.recyclerview.widget.DiffUtil

class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
    override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean =
        oldItem.ids == newItem.ids


    override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean =
        oldItem == newItem
}

class PairIntStringDiffCallback : DiffUtil.ItemCallback<Pair<Int, String>>() {
    override fun areItemsTheSame(oldItem: Pair<Int, String>, newItem: Pair<Int, String>): Boolean =
        oldItem.first == newItem.first


    override fun areContentsTheSame(
        oldItem: Pair<Int, String>,
        newItem: Pair<Int, String>
    ): Boolean =
        oldItem == newItem
}