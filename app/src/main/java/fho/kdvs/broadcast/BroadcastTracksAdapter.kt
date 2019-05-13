package fho.kdvs.broadcast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellAirbreakBinding
import fho.kdvs.databinding.CellTrackBinding
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData

/** A [BindingRecyclerViewAdapter] which recycles track cells and airbreak cells. */
class BroadcastTracksAdapter(
    private val viewModel: BroadcastDetailsViewModel,
    private val sharedViewModel: SharedViewModel,
    onClick: (ClickData<TrackEntity>) -> Unit) :
    BindingRecyclerViewAdapter<TrackEntity, BindingViewHolder<TrackEntity>>(onClick, TrackDiffCallback()
){
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).airbreak) VIEW_TYPE_AIRBREAK else VIEW_TYPE_TRACK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<TrackEntity> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_AIRBREAK -> {
                val binding = fho.kdvs.databinding.CellAirbreakBinding.inflate(inflater, parent, false)
                AirbreakViewHolder(binding)
            }
            else -> {
                val binding = fho.kdvs.databinding.CellTrackBinding.inflate(inflater, parent, false)
                TrackViewHolder(binding, viewModel, sharedViewModel)
            }
        }
    }

    class TrackViewHolder(
        private val binding: CellTrackBinding,
        private val viewModel: BroadcastDetailsViewModel,
        private val sharedViewModel: SharedViewModel
    ) : BindingViewHolder<TrackEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: TrackEntity) {
            binding.apply {
                track = item
                clickListener = listener
                vm = viewModel
                sharedVm = sharedViewModel
                favorited = viewModel.favoritedTracks.contains(item.trackId)
            }
        }
    }

    class AirbreakViewHolder(binding: CellAirbreakBinding) : BindingViewHolder<TrackEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: TrackEntity) {}
    }

    fun onTracksChanged(tracks: List<TrackEntity>) {
        submitList(tracks)
    }

    companion object {
        const val VIEW_TYPE_AIRBREAK = 0
        const val VIEW_TYPE_TRACK = 1
    }
}