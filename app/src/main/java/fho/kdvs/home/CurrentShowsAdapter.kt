package fho.kdvs.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellCurrentShowBinding
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.show.ShowDiffCallback

class CurrentShowsAdapter(
    private val viewModel: HomeViewModel,
    onClick: (ClickData<ShowEntity>) -> Unit
) :
    BindingRecyclerViewAdapter<ShowEntity, BindingViewHolder<ShowEntity>>(
        onClick,
        ShowDiffCallback()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<ShowEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellCurrentShowBinding.inflate(inflater, parent, false)
        return ShowViewHolder(binding, viewModel)
    }

    override fun onViewAttachedToWindow(holder: BindingViewHolder<ShowEntity>) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.requestFocus()
    }

    class ShowViewHolder(
        private val binding: CellCurrentShowBinding,
        private val viewModel: HomeViewModel
    ) :
        BindingViewHolder<ShowEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowEntity) {
            binding.apply {
                vm = viewModel
                currentShow = item
                clickListener = listener
            }
        }
    }

    fun onCurrentShowsChanged(shows: List<ShowEntity>) {
        submitList(shows)
    }
}