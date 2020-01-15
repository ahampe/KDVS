package fho.kdvs.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellCurrentShowBinding
import fho.kdvs.global.database.ShowTimeslotEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.show.ShowTimeslotDiffCallback

class CurrentShowsAdapter(
    private val viewModel: HomeViewModel,
    onClick: (ClickData<ShowTimeslotEntity>) -> Unit
) :
    BindingRecyclerViewAdapter<ShowTimeslotEntity, BindingViewHolder<ShowTimeslotEntity>>(
        onClick,
        ShowTimeslotDiffCallback()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<ShowTimeslotEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellCurrentShowBinding.inflate(inflater, parent, false)
        return ShowViewHolder(binding, viewModel)
    }

    override fun onViewAttachedToWindow(holder: BindingViewHolder<ShowTimeslotEntity>) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.requestFocus()
    }

    class ShowViewHolder(
        private val binding: CellCurrentShowBinding,
        private val viewModel: HomeViewModel
    ) :
        BindingViewHolder<ShowTimeslotEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowTimeslotEntity) {
            binding.apply {
                vm = viewModel
                currentShow = item
                clickListener = listener
            }
        }
    }

    fun onCurrentShowsChanged(shows: List<ShowTimeslotEntity>) {
        submitList(shows)
    }
}