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
class ShowSearchViewAdapter(
    private val showsWithTimeSlotSize: List<Pair<ShowEntity, Int>>,
    private val fragment: ShowSearchFragment,
    onClick: (ClickData<ShowEntity>) -> Unit
) : BindingRecyclerViewAdapter<ShowEntity, ShowSearchViewAdapter.ViewHolder>(onClick, ShowDiffCallback()), Filterable {

    var query: String = ""

    private var shows: List<ShowEntity>? = null
    private var showsFiltered: List<ShowEntity>? = null

    init {
        this.shows = showsWithTimeSlotSize
            .map { s -> s.first }
            .toList()
            .distinct()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellShowSearchResultBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, showsWithTimeSlotSize, query)
    }
    
    override fun getItemCount(): Int {
        return showsFiltered?.size ?: 0
    }

    /** Filters show names containing query at start of string, both with and without articles, case insensitive. */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<ShowEntity>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()){
                    if (fragment.hashedShows[query] != null) {
                        filteredList.addAll(fragment.hashedShows[query]!!)
                    } else {
                        shows?.forEach {
                            //
                            if ("^$query".toRegex() // with articles
                                    .find(it.name?.toLowerCase() ?: "") != null ||
                                "^$query".toRegex() // without articles
                                    .find(removeArticles(it.name?.toLowerCase())) != null)
                                filteredList.add(it)
                        }
                        showsFiltered = filteredList
                        fragment.hashedShows[query] = filteredList
                    }
                } else {
                    showsFiltered = mutableListOf()
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                filterResults.count = filteredList.size

                return filterResults
            }

            @SuppressWarnings("unchecked")
            override fun publishResults(charSeq: CharSequence, results: FilterResults) {
                Timber.d("${results.count} results found")
                if (results.values is List<*>) {
                    showsFiltered = results.values as? List<ShowEntity>? // TODO: safe cast?

                    // alphabetical sort ignoring leading articles
                    submitList(showsFiltered?.sortedBy { s ->
                        removeArticles(s.name?.toLowerCase()?.trim()) })
                }
            }
        }
    }

    private fun removeArticles(str: String?): String {
        return """^(?:(the|a|an) +)""".toRegex()
            .replace(str ?: "", "")
    }

    class ViewHolder(
        private val binding: CellShowSearchResultBinding,
        private val showsWithTimeSlotSize: List<Pair<ShowEntity, Int>>,
        private val queryStr: String
    ) : BindingViewHolder<ShowEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowEntity) {
            binding.apply {
                clickListener = listener
                show = item
                timeSlotSize = showsWithTimeSlotSize
                    .firstOrNull { s -> s.first == item }
                    ?.second
                query = queryStr
            }
        }
    }
}