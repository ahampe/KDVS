package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellScheduleSearchResultBinding
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.show.ShowDiffCallback

/** Adapter for a show search view.*/
class ScheduleSearchViewAdapter(onClick: (ClickData<ShowEntity>) -> Unit) :
    BindingRecyclerViewAdapter<ShowEntity, ScheduleSearchViewAdapter.ViewHolder>(onClick, ShowDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellScheduleSearchResultBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(private val binding: CellScheduleSearchResultBinding) : BindingViewHolder<ShowEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowEntity) {
            binding.apply {
                clickListener = listener
                show = item
            }
        }
    }
}