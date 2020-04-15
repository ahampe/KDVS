package fho.kdvs.modules.broadcast

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.global.database.TrackEntity

class TrackDiffCallback : DiffUtil.ItemCallback<TrackEntity>() {
    override fun areItemsTheSame(oldItem: TrackEntity, newItem: TrackEntity) =
        oldItem.trackId == newItem.trackId

    override fun areContentsTheSame(oldItem: TrackEntity, newItem: TrackEntity) =
        oldItem == newItem
}