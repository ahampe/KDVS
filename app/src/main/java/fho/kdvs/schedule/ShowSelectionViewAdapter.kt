package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellShowSelectionBinding
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData

/** Adapter for a show selection view.*/
class ShowSelectionViewAdapter(onClick: (ClickData<Pair<Int, String>>) -> Unit) :
    BindingRecyclerViewAdapter<Pair<Int, String>, ShowSelectionViewAdapter.ViewHolder>(
        onClick,
        PairIntStringDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellShowSelectionBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(private val binding: CellShowSelectionBinding) :
        BindingViewHolder<Pair<Int, String>>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: Pair<Int, String>) {
            binding.apply {
                id = item.first
                name = item.second
                index = adapterPosition
                clickListener = listener
            }
        }
    }
}