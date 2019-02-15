package fho.kdvs.show

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.global.database.BroadcastEntity

class BroadcastDiffCallback : DiffUtil.ItemCallback<BroadcastEntity>() {
    override fun areItemsTheSame(oldItem: BroadcastEntity, newItem: BroadcastEntity) =
        oldItem.broadcastId == newItem.broadcastId

    override fun areContentsTheSame(oldItem: BroadcastEntity, newItem: BroadcastEntity) =
        oldItem == newItem
}