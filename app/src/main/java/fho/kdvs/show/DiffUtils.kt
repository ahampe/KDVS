package fho.kdvs.show

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.database.ShowEntity

class BroadcastDiffCallback : DiffUtil.ItemCallback<BroadcastEntity>() {
    override fun areItemsTheSame(oldItem: BroadcastEntity, newItem: BroadcastEntity) =
        oldItem.broadcastId == newItem.broadcastId

    override fun areContentsTheSame(oldItem: BroadcastEntity, newItem: BroadcastEntity) =
        oldItem == newItem
}

class ShowDiffCallback : DiffUtil.ItemCallback<ShowEntity>() {
    override fun areItemsTheSame(oldItem: ShowEntity, newItem: ShowEntity) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ShowEntity, newItem: ShowEntity) =
        oldItem == newItem
}