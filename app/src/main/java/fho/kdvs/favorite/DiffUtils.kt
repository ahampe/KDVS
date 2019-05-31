package fho.kdvs.favorite

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.global.database.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.database.getFavorite

class FavoriteTrackDiffCallback : DiffUtil.ItemCallback<ShowBroadcastTrackFavoriteJoin>() {
    override fun areItemsTheSame(oldItem: ShowBroadcastTrackFavoriteJoin, newItem: ShowBroadcastTrackFavoriteJoin) =
        oldItem.getFavorite()?.favoriteId == newItem.getFavorite()?.favoriteId

    override fun areContentsTheSame(oldItem: ShowBroadcastTrackFavoriteJoin, newItem: ShowBroadcastTrackFavoriteJoin) =
        oldItem == newItem
}