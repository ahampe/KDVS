package fho.kdvs.home

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.global.database.ContactEntity
import fho.kdvs.global.database.NewsEntity
import fho.kdvs.global.database.TopMusicEntity

class NewsDiffCallback : DiffUtil.ItemCallback<NewsEntity>() {
    override fun areItemsTheSame(oldItem: NewsEntity, newItem: NewsEntity): Boolean =
        oldItem.title == newItem.title && oldItem.date == newItem.date

    override fun areContentsTheSame(oldItem: NewsEntity, newItem: NewsEntity): Boolean =
        oldItem == newItem
}

class TopMusicDiffCallback : DiffUtil.ItemCallback<TopMusicEntity>() {
    override fun areItemsTheSame(oldItem: TopMusicEntity, newItem: TopMusicEntity): Boolean =
        oldItem.artist == newItem.artist && oldItem.album == newItem.album && oldItem.weekOf == newItem.weekOf

    override fun areContentsTheSame(oldItem: TopMusicEntity, newItem: TopMusicEntity): Boolean =
        oldItem == newItem
}

class ContactDiffCallback : DiffUtil.ItemCallback<ContactEntity>() {
    override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean =
        oldItem.name == newItem.name && oldItem.position == newItem.position

    override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean =
        oldItem == newItem
}