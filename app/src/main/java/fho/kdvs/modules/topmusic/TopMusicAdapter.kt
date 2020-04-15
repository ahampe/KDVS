package fho.kdvs.modules.topmusic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellTopmusicBinding
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.home.TopMusicDiffCallback

class TopMusicAdapter(onClick: (ClickData<TopMusicEntity>) -> Unit) :
    BindingRecyclerViewAdapter<TopMusicEntity, BindingViewHolder<TopMusicEntity>>(
        onClick,
        TopMusicDiffCallback()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<TopMusicEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellTopmusicBinding.inflate(inflater, parent, false)
        return TopMusicViewHolder(binding)
    }

    class TopMusicViewHolder(private val binding: CellTopmusicBinding) :
        BindingViewHolder<TopMusicEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: TopMusicEntity) {
            binding.apply {
                topMusic = item
                clickListener = listener
            }
        }
    }

    fun onTopAddsChanged(topAdds: List<TopMusicEntity>) {
        submitList(topAdds)
    }

    fun onTopAlbumsChanged(topAlbums: List<TopMusicEntity>) {
        submitList(topAlbums)
    }
}