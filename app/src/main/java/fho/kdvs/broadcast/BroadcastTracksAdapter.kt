package fho.kdvs.broadcast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellTrackBinding
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData

class BroadcastTracksAdapter(onClick: (ClickData<TrackEntity>) -> Unit) :
    BindingRecyclerViewAdapter<TrackEntity, BroadcastTracksAdapter.ViewHolder>(onClick, TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = fho.kdvs.databinding.CellTrackBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(private val binding: CellTrackBinding) : BindingViewHolder<TrackEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: TrackEntity) {
            binding.apply {
                track = item
            }
        }
    }

    fun onTracksChanged(tracks: List<TrackEntity>) {
        submitList(tracks)
    }
}