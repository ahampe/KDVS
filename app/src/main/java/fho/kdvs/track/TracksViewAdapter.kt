package fho.kdvs.track

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.databinding.CellTrackDetailsBinding
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.home.TrackDiffCallback

/** A [RecyclerView.Adapter] which cycles through [TrackEntity] items */
@kotlinx.serialization.UnstableDefault
class TracksViewAdapter(onClick: (ClickData<TrackEntity>) -> Unit) :
    BindingRecyclerViewAdapter<TrackEntity, BindingViewHolder<TrackEntity>>(onClick, TrackDiffCallback()){

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<TrackEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellTrackDetailsBinding.inflate(inflater, parent, false)
        return TrackViewHolder(binding)
    }

    class TrackViewHolder(private val binding: CellTrackDetailsBinding) :
        BindingViewHolder<TrackEntity>(binding.root){
        override fun bind(listener: View.OnClickListener, item: TrackEntity) {
            binding.apply {
                trackData = item
            }
        }
    }

    fun onTracksChanged(topMusic: List<TrackEntity>) {
        submitList(topMusic)
    }
}