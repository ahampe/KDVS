package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellShowSearchResultBinding
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.show.ShowDiffCallback
import timber.log.Timber

/** Adapter for a show search view.*/
class ShowSearchViewAdapter(val shows: List<ShowEntity>, onClick: (ClickData<ShowEntity>) -> Unit) :
    BindingRecyclerViewAdapter<ShowEntity, ShowSearchViewAdapter.ViewHolder>(onClick, ShowDiffCallback()), Filterable {
    
    private var showSearchResults: List<ShowEntity>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellShowSearchResultBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return showSearchResults?.size ?: 0
    }

    /** Filter is based off individual words in show names.*/
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val filteredList = ArrayList<ShowEntity>()
                val charString = charSequence.toString()
                if (charString.isNotEmpty()){
                    shows.forEach {// TODO: hash show names
                        val words = it.name?.split(' ')?.toList()
                        words?.forEach word@{ w ->
                            if (w.toLowerCase().contains(charString.toLowerCase())) { // TODO: only look at first m characters
                                filteredList.add(it)
                                return@word
                            }
                        }
                    }
                    showSearchResults = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                filterResults.count = filteredList.size
                return filterResults
            }

            @SuppressWarnings("unchecked")
            override fun publishResults(charSequence: CharSequence, results: FilterResults) {
                if (results.count > 0) {
                    Timber.d("${results.count} results found")
                    if (results.values is List<*>) {
                        showSearchResults = results.values as? List<ShowEntity>? // TODO: safe cast?
                        onResultsChanged()
                    }
                    notifyDataSetChanged()
                } else {
                    Timber.d("0 results found")
                }
            }
        }
    }

    private fun onResultsChanged() {
        submitList(showSearchResults?.sortedBy { s -> s.name })
    }

    class ViewHolder(private val binding: CellShowSearchResultBinding) : BindingViewHolder<ShowEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowEntity) {
            binding.apply {
                clickListener = listener
                show = item
            }
        }
    }
}