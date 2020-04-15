package fho.kdvs.modules.show

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellBroadcastBinding
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.global.util.TimeHelper

class ShowBroadcastsAdapter(onClick: (ClickData<BroadcastEntity>) -> Unit) :
    BindingRecyclerViewAdapter<BroadcastEntity, ShowBroadcastsAdapter.ViewHolder>(
        onClick,
        BroadcastDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellBroadcastBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(private val binding: CellBroadcastBinding) :
        BindingViewHolder<BroadcastEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: BroadcastEntity) {
            binding.apply {
                clickListener = listener
                broadcast = item
                dateFormatter = TimeHelper.uiDateFormatter
            }
        }
    }

    fun onBroadcastsChanged(broadcasts: List<BroadcastEntity>) {
        submitList(broadcasts)
    }
}