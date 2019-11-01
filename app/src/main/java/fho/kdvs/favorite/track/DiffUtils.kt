package fho.kdvs.favorite.track

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.favorite.track.FavoriteTrackJoin

class FavoriteTrackDiffCallback : DiffUtil.ItemCallback<FavoriteTrackJoin>() {
    override fun areItemsTheSame(oldItem: FavoriteTrackJoin, newItem: FavoriteTrackJoin) =
        oldItem.favorite == newItem.favorite

    override fun areContentsTheSame(oldItem: FavoriteTrackJoin, newItem: FavoriteTrackJoin) =
        oldItem == newItem
}