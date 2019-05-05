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
class ShowSearchViewAdapter(private val shows: List<ShowEntity>, onClick: (ClickData<ShowEntity>) -> Unit) :
    BindingRecyclerViewAdapter<ShowEntity, ShowSearchViewAdapter.ViewHolder>(onClick, ShowDiffCallback()), Filterable {

    var query: String? = null
    private val showsFiltered = mutableListOf<ShowEntity>()

    init {
        showsFiltered.addAll(shows)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellShowSearchResultBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, query)
    }
    
    override fun getItemCount(): Int {
        return showsFiltered.size
    }

    override fun getItem(position: Int): ShowEntity {
        return showsFiltered[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /** Filters show names containing query, case insensitive. */
    override fun getFilter(): Filter { // TODO: hash results
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<ShowEntity>()
                val charString = charSeq.toString().trim()

                if (charString.isNotEmpty()){
                    shows.forEach {
                        if ((it.name ?: "" ).toLowerCase()
                            .contains(charString.toLowerCase()))
                            filteredList.add(it)
                    }
                    query = charString
                    showsFiltered.clear()
                    showsFiltered.addAll(filteredList)
                } else {
                    showsFiltered.clear()
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                filterResults.count = filteredList.size

                return filterResults
            }

            override fun publishResults(charSeq: CharSequence, results: FilterResults) {
                Timber.d("${results.count} results found")
                showsFiltered.clear()
                showsFiltered.addAll(results.values as Iterable<ShowEntity>)  // TODO: safe cast?
//                    ?.sortedBy { v -> v.name
//                        ?.toLowerCase()
//                        ?.replace("the", "")
//                        ?.trim()
//                    })
                //onResultsChanged()
                //submitList(showsFiltered)

                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(
        private val binding: CellShowSearchResultBinding,
        private val queryStr: String?
    ) : BindingViewHolder<ShowEntity>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: ShowEntity) {
            binding.apply {
                clickListener = listener
                show = item
                timeSlotSize = 1 // TODO: pass in count of timeslot shows
//                timeSlotSize = showsWithTimeSlotSize
//                    .firstOrNull { s -> s.first == item }
//                    ?.second
                query = queryStr // TODO: highlight query in cell
            }
        }
    }
}