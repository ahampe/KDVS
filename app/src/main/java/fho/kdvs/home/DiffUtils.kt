package fho.kdvs.home

import androidx.recyclerview.widget.DiffUtil
import fho.kdvs.global.database.*

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

class TrackDiffCallback : DiffUtil.ItemCallback<TrackEntity>() {
    override fun areItemsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean =
        oldItem.artist == newItem.artist && oldItem.album == newItem.album

    override fun areContentsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean =
        oldItem == newItem
}

class StaffDiffCallback : DiffUtil.ItemCallback<StaffEntity>() {
    override fun areItemsTheSame(oldItem: StaffEntity, newItem: StaffEntity): Boolean =
        oldItem.name == newItem.name && oldItem.position == newItem.position

    override fun areContentsTheSame(oldItem: StaffEntity, newItem: StaffEntity): Boolean =
        oldItem == newItem
}

class FundraiserDiffCallback : DiffUtil.ItemCallback<FundraiserEntity>() {
    override fun areItemsTheSame(oldItem: FundraiserEntity, newItem: FundraiserEntity): Boolean =
        oldItem.dateStart == newItem.dateStart && oldItem.dateEnd == newItem.dateEnd

    override fun areContentsTheSame(oldItem: FundraiserEntity, newItem: FundraiserEntity): Boolean =
        oldItem == newItem
}