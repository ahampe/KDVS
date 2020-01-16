package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellShowSearchResultBinding
import fho.kdvs.global.database.ShowTimeslotEntity
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.show.ShowTimeslotDiffCallback
import timber.log.Timber

/** Adapter for a show search view.*/
class ShowSearchViewAdapter(
    private val showTimeslotsWithSize: List<Pair<ShowTimeslotEntity, Int>>,
    private val fragment: ShowSearchFragment,
    onClick: (ClickData<ShowTimeslotEntity>) -> Unit
) : BindingRecyclerViewAdapter<ShowTimeslotEntity, ShowSearchViewAdapter.ViewHolder>(
    onClick,
    ShowTimeslotDiffCallback()
), Filterable {

    var query: String = ""

    var shows: List<ShowTimeslotEntity>? = null
    val results = mutableListOf<ShowTimeslotEntity>()

    init {
        shows = showTimeslotsWithSize
            .map { s -> s.first }
            .toList()
            .distinct()

        shows?.let {
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
                val filteredList = ArrayList<ShowTimeslotEntity>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()) {
                    if (fragment.hashedShows[query] != null) {
                        filteredList.addAll(fragment.hashedShows[query]!!)
                    } else {
                        shows?.forEach {
                            if ("^$query".toRegex() // with articles
                                    .find(it.name?.toLowerCase() ?: "") != null ||
                                "^$query".toRegex() // without articles
                                    .find(
                                        it.name?.toLowerCase()?.removeLeadingArticles() ?: ""
                                    ) != null
                            )
                                filteredList.add(it)
                        }

                        results.clear()
                        results.addAll(filteredList)

                        fragment.hashedShows[query] = filteredList
                    }
                } else {
                    results.clear()

                    shows?.let {
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
        submitList(this@ShowSearchViewAdapter.results.sortedBy { s ->
            s.name
                ?.toLowerCase()
                ?.trim()
                ?.removeLeadingArticles()
        })
    }

    class ViewHolder(
        private val binding: CellShowSearchResultBinding,
        private val queryStr: String
    ) : BindingViewHolder<ShowTimeslotEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowTimeslotEntity) {
            binding.apply {
                clickListener = listener
                showTimeslot = item
                query = queryStr
            }
        }
    }
}
