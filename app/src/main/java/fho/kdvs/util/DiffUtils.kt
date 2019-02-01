package fho.kdvs.util

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.model.database.entities.ShowEntity

class ShowDiffCallback : DiffUtil.ItemCallback<ShowEntity>() {
    override fun areItemsTheSame(oldItem: ShowEntity, newItem: ShowEntity) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ShowEntity, newItem: ShowEntity) =
        oldItem == newItem

    override fun getChangePayload(oldItem: ShowEntity, newItem: ShowEntity): Any? {
        return super.getChangePayload(oldItem, newItem) // TODO
    }
}
