package fho.kdvs.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellArticleBinding
import fho.kdvs.global.database.NewsEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.home.NewsDiffCallback

class NewsArticlesAdapter(onClick: (ClickData<NewsEntity>) -> Unit) :
    BindingRecyclerViewAdapter<NewsEntity, BindingViewHolder<NewsEntity>>(onClick, NewsDiffCallback()){

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<NewsEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = fho.kdvs.databinding.CellArticleBinding.inflate(inflater, parent, false)
        return ArticleViewHolder(binding)
    }

    class ArticleViewHolder(private val binding: CellArticleBinding) : BindingViewHolder<NewsEntity>(binding.root){
        override fun bind(listener: View.OnClickListener, item: NewsEntity) {
            binding.apply {
                clickListener = listener
                article = item
                dateFormatter = TimeHelper.uiDateFormatter
            }
        }
    }

    fun onNewsChanged(articles: List<NewsEntity>) {
        submitList(articles)
    }
}