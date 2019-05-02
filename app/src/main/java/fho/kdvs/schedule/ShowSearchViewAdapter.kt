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
import kotlin.math.min

/** Adapter for a show search view.*/
class ShowSearchViewAdapter(val showsWithTimeSlotSize: List<Pair<ShowEntity, Int>>, onClick: (ClickData<ShowEntity>) -> Unit) :
    BindingRecyclerViewAdapter<ShowEntity, ShowSearchViewAdapter.ViewHolder>(onClick, ShowDiffCallback()), Filterable {

    private var showNames: List<ShowEntity>? = null
    private var showNamesFiltered: List<ShowEntity>? = null

    init {
        this.showNames = showsWithTimeSlotSize.map { s -> s.first }.toList()
        //this.showNamesFiltered = this.showNames
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellShowSearchResultBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, showsWithTimeSlotSize)
    }
    
    override fun getItemCount(): Int {
        return showNamesFiltered?.size ?: 0
    }

    /** Filters on matches of substrings starting from the beginning of words (word defined as query split by spaces).
     * At least one word from the query must match at least one substring of a word from the show name, starting
     * from index 0. e.g. Queries of "sui", "the", "th wa", "the watch" will all return "The Suicide Watch". */
    override fun getFilter(): Filter { // TODO: hash results
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<ShowEntity>()
                val charString = charSeq.toString().trim()

                if (charString.isNotEmpty()){
                    showNames?.forEach {
                        val queryWords = charString.split(' ').toList()
                        val showWords = it.name?.split(' ')?.toList()
                        showWords?.forEach showWord@{ sw ->
                            queryWords.forEach queryWord@{ qw ->
                                if (sw.toLowerCase().substring(0, min(qw.length, sw.length)) == qw.toLowerCase()) {
                                    filteredList.add(it)
                                    return@showWord
                                }
                            }
                        }
                    }
                    showNamesFiltered = filteredList
                } else {
                    showNamesFiltered = mutableListOf()
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
                    showNamesFiltered = results.values as? List<ShowEntity>? // TODO: safe cast?
                    onResultsChanged()
                }
                //notifyDataSetChanged()
            }
        }
    }

    private fun onResultsChanged() {
        submitList(showNamesFiltered?.sortedBy { s -> s.name })
    }

    class ViewHolder(
        private val binding: CellShowSearchResultBinding,
        private val showsWithTimeSlotSize: List<Pair<ShowEntity, Int>>
    ) : BindingViewHolder<ShowEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowEntity) {
            binding.apply {
                clickListener = listener
                show = item
                timeSlotSize = showsWithTimeSlotSize
                    .firstOrNull { s -> s.first == item }
                    ?.second
            }
        }
    }
}