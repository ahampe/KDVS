package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellTimeslotBinding
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder

/** Adapter for a single timeslot card. */
class TimeSlotViewAdapter :
    BindingRecyclerViewAdapter<TimeSlot, TimeSlotViewAdapter.ViewHolder>(TimeSlotDiffCallback()) {

    class ViewHolder(private val binding: CellTimeslotBinding) : BindingViewHolder<TimeSlot>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: TimeSlot) {
//            Glide.with(binding.root)
//                .applyDefaultRequestOptions(
//                    RequestOptions()
//                        .error(R.drawable.show_placeholder)
//                        .apply(RequestOptions.centerCropTransform())
//                )
//                .load(item.imageHref)
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(binding.showImage)

            // TODO set height in binding based on show duration
            binding.apply {
                clickListener = listener
                timeslot = item
            }
        }
    }

    fun onShowsChanged(shows: List<TimeSlot>) {
        submitList(shows)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellTimeslotBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }
}