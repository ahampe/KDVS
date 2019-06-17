package fho.kdvs.topmusic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.databinding.CellTopmusicDetailsBinding
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.home.TopMusicDiffCallback

/** A [RecyclerView.Adapter] which cycles through [TopMusicEntity] items */
@kotlinx.serialization.UnstableDefault
class TopMusicViewAdapter(onClick: (ClickData<TopMusicEntity>) -> Unit) :
    BindingRecyclerViewAdapter<TopMusicEntity, BindingViewHolder<TopMusicEntity>>(onClick, TopMusicDiffCallback()){

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<TopMusicEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellTopmusicDetailsBinding.inflate(inflater, parent, false)
        return TopMusicViewHolder(binding)
    }

    class TopMusicViewHolder(private val binding: CellTopmusicDetailsBinding) :
        BindingViewHolder<TopMusicEntity>(binding.root){
        override fun bind(listener: View.OnClickListener, item: TopMusicEntity) {
            binding.apply {
                topMusicData = item
            }
        }
    }

    fun onTopMusicChanged(topMusic: List<TopMusicEntity>) {
        submitList(topMusic)
    }
}