package fho.kdvs.favorite

import androidx.recyclerview.widget.DiffUtil
class FavoriteTrackDiffCallback : DiffUtil.ItemCallback<FavoriteJoin>() {
    override fun areItemsTheSame(oldItem: FavoriteJoin, newItem: FavoriteJoin) =
        oldItem.favorite == newItem.favorite

    override fun areContentsTheSame(oldItem:  FavoriteJoin, newItem:  FavoriteJoin) =
        oldItem == newItem
}