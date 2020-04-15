package fho.kdvs.modules.favorite.broadcast

import androidx.recyclerview.widget.DiffUtil

class FavoriteBroadcastDiffCallback : DiffUtil.ItemCallback<FavoriteBroadcastJoin>() {
    override fun areItemsTheSame(oldItem: FavoriteBroadcastJoin, newItem: FavoriteBroadcastJoin) =
        oldItem.favorite == newItem.favorite

    override fun areContentsTheSame(
        oldItem: FavoriteBroadcastJoin,
        newItem: FavoriteBroadcastJoin
    ) =
        oldItem == newItem
}