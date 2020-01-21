package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellShowSearchResultBinding
import fho.kdvs.global.database.joins.ShowTimeslotsJoin
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.show.ShowTimeslotsJoinDiffCallback
import timber.log.Timber


class ShowSearchViewAdapter (
    joins: List<ShowTimeslotsJoin>,
    private val fragment: ShowSearchFragment,
    onClick: (ClickData<ShowTimeslotsJoin>) -> Unit
) : BindingRecyclerViewAdapter<ShowTimeslotsJoin, ShowSearchViewAdapter.ViewHolder>(
    onClick,
    ShowTimeslotsJoinDiffCallback()
), Filterable {
    var query: String = ""

    var showTimeslotsJoins: List<ShowTimeslotsJoin>? = null
    val results = mutableListOf<ShowTimeslotsJoin>()

    init {
        showTimeslotsJoins = joins.distinct()

        showTimeslotsJoins?.let {
            results.addAll(it)
            submitResults()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellShowSearchResultBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, query)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    /** Filters show names containing query at start of string, both with and without articles, case insensitive. */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<ShowTimeslotsJoin>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()) {
                    if (fragment.hashedShowTimeslotsJoins[query] != null) {
                        filteredList.addAll(fragment.hashedShowTimeslotsJoins[query]!!)
                    } else {
                        showTimeslotsJoins?.forEach {
                            if ("^$query".toRegex() // with articles
                                    .find(it.show?.name?.toLowerCase() ?: "") != null ||
                                "^$query".toRegex() // without articles
                                    .find(
                                        it.show?.name?.toLowerCase()?.removeLeadingArticles() ?: ""
                                    ) != null
                            )
                                filteredList.add(it)
                        }

                        results.clear()
                        results.addAll(filteredList)

                        fragment.hashedShowTimeslotsJoins[query] = filteredList
                    }
                } else {
                    results.clear()

                    showTimeslotsJoins?.let {
                        results.addAll(it)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                filterResults.count = filteredList.size

                return filterResults
            }

            @SuppressWarnings("unchecked")
            override fun publishResults(charSeq: CharSequence, results: FilterResults) {
                Timber.d("${results.count} currentlyDisplayingResults found")
                if (results.values is List<*>) {
                    submitResults()
                }
            }
        }
    }

    fun submitResults() {
        submitList(this@ShowSearchViewAdapter.results.sortedBy { r ->
            r.show?.name
                ?.toLowerCase()
                ?.trim()
                ?.removeLeadingArticles()
        })
    }

    class ViewHolder(
        private val binding: CellShowSearchResultBinding,
        private val queryStr: String
    ) : BindingViewHolder<ShowTimeslotsJoin>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowTimeslotsJoin) {
            binding.apply {
                clickListener = listener
                show = item.show
                query = queryStr
            }
        }
    }
}
