package fho.kdvs.favorite.broadcast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellFavoriteBroadcastBinding
import fho.kdvs.favorite.FavoriteFragment.SortDirection
import fho.kdvs.favorite.FavoriteFragment.SortType
import fho.kdvs.global.database.ShowBroadcastFavoriteJoin
import fho.kdvs.global.database.getBroadcastFavoriteJoins
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.global.util.TimeHelper
import timber.log.Timber


class FavoriteBroadcastViewAdapter(
    broadcastJoins: List<ShowBroadcastFavoriteJoin>?,
    private val fragment: FavoriteBroadcastFragment,
    onClick: (ClickData<FavoriteBroadcastJoin>) -> Unit
) : BindingRecyclerViewAdapter<FavoriteBroadcastJoin, FavoriteBroadcastViewAdapter.ViewHolder>(
    onClick,
    FavoriteBroadcastDiffCallback()
), Filterable {
    var query: String = ""
    val results = mutableListOf<FavoriteBroadcastJoin>()
    var allFavorites = mutableListOf<FavoriteBroadcastJoin>()

    init {
        val broadcastFavoriteJoins = broadcastJoins.getBroadcastFavoriteJoins()

        broadcastFavoriteJoins?.let {
            results.addAll(it)
        }

        allFavorites.addAll(results)

        updateFragmentResults()

        submitList(results)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellFavoriteBroadcastBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, query)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<FavoriteBroadcastJoin>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()) {
                    if (!fragment.hashedResults[query].isNullOrEmpty()) {
                        filteredList.addAll(fragment.hashedResults[query]!!)
                        results.clear()
                        results.addAll(filteredList)
                    } else {
                        allFavorites.forEach { join ->
                            val fieldsToSearch = listOf(
                                join.show?.name
                            )

                            fieldsToSearch.forEach { field ->
                                field?.let {
                                    if (!filteredList.contains(join) &&
                                        "^$query".toRegex()
                                            .find(it.toLowerCase().removeLeadingArticles()) != null
                                    )
                                        filteredList.add(join)
                                }
                            }
                        }

                        results.clear()
                        results.addAll(filteredList)
                        fragment.hashedResults[query] = filteredList
                    }
                } else {
                    results.clear()
                    results.addAll(allFavorites)
                }

                val filterResults = FilterResults()
                filterResults.values = results
                filterResults.count = results.size

                return filterResults
            }

            @SuppressWarnings("unchecked")
            override fun publishResults(charSeq: CharSequence, filterResults: FilterResults) {
                Timber.d("${filterResults.count} results found")
                updateData()
            }
        }
    }

    fun updateData() {
        val sortDirection = fragment.sortDirection
        val sortType = fragment.sortType

        val newResults = (when (sortDirection) {
            SortDirection.ASC -> when (sortType) {
                SortType.RECENT -> results.sortedBy { it.favorite?.favoriteBroadcastId }
                SortType.SHOW -> results.sortedBy {
                    it.show?.name?.formatName()
                }
                SortType.DATE -> results.sortedByDescending {
                    it.broadcast?.date
                }
                else -> results
            }
            SortDirection.DES -> when (sortType) {
                SortType.RECENT -> results.sortedByDescending { it.favorite?.favoriteBroadcastId }
                SortType.SHOW -> results.sortedByDescending {
                    it.show?.name?.formatName()
                }
                SortType.DATE -> results.sortedBy {
                    it.broadcast?.date
                }
                else -> results
            }
        })

        newResults.let {
            results.clear()
            results.addAll(it)

            updateFragmentResults()

            notifyDataSetChanged()
        }
    }

    private fun updateFragmentResults() {
        fragment.currentlyDisplayingResults.clear()
        fragment.currentlyDisplayingResults.addAll(results)
    }

    private fun String?.formatName() = this?.toUpperCase().removeLeadingArticles()

    class ViewHolder(
        private val binding: CellFavoriteBroadcastBinding,
        private val queryStr: String
    ) : BindingViewHolder<FavoriteBroadcastJoin>(binding.root) {

        override fun bind(listener: View.OnClickListener, item: FavoriteBroadcastJoin) {
            binding.apply {
                clickListener = listener
                broadcast = item.broadcast
                show = item.show
                query = queryStr
                dateFormatter = TimeHelper.uiDateFormatter
            }
        }
    }
}
