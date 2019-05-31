package fho.kdvs.favorite

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.global.database.FavoriteEntity
import fho.kdvs.global.database.TrackEntity

class FavoriteTrackDiffCallback : DiffUtil.ItemCallback<Pair<FavoriteEntity?, TrackEntity?>>() {
    override fun areItemsTheSame(oldItem: Pair<FavoriteEntity?, TrackEntity?>, newItem: Pair<FavoriteEntity?, TrackEntity?>) =
        oldItem.first?.favoriteId == newItem.first?.favoriteId

    override fun areContentsTheSame(oldItem:  Pair<FavoriteEntity?, TrackEntity?>, newItem:  Pair<FavoriteEntity?, TrackEntity?>) =
        oldItem == newItem
}