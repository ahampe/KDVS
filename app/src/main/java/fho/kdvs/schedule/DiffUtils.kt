package fho.kdvs.schedule

import androidx.recyclerview.widget.DiffUtil

class TimeSlotDiffCallback : DiffUtil.ItemCallback<ScheduleTimeslot>() {
    override fun areItemsTheSame(oldItem: ScheduleTimeslot, newItem: ScheduleTimeslot): Boolean =
        oldItem.ids == newItem.ids


    override fun areContentsTheSame(oldItem: ScheduleTimeslot, newItem: ScheduleTimeslot): Boolean =
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