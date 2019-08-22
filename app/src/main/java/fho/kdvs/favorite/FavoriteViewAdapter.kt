package fho.kdvs.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellFavoriteTrackBinding
import fho.kdvs.global.database.*
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import timber.log.Timber

class FavoriteViewAdapter(
    joins: List<ShowBroadcastTrackFavoriteJoin>?,
    private val fragment: FavoriteFragment,
    onClick: (ClickData<FavoriteJoin>) -> Unit
) : BindingRecyclerViewAdapter<FavoriteJoin, FavoriteViewAdapter.ViewHolder>(onClick, FavoriteTrackDiffCallback()), Filterable {

    var query: String = ""

    val results = mutableListOf<FavoriteJoin>()
    var allFavorites = mutableListOf<FavoriteJoin>()
    
    init {
        val favoriteJoins = joins.getFavoriteJoins()

        favoriteJoins?.let {
            results.addAll(it)
        }

        allFavorites.addAll(results)

        submitList(results)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellFavoriteTrackBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, query)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<FavoriteJoin>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()){
                    if (!fragment.hashedResults[query].isNullOrEmpty()) {
                        filteredList.addAll(fragment.hashedResults[query]!!)
                        results.clear()
                        results.addAll(filteredList)
                    } else {
                        allFavorites.forEach { join ->
                            val fieldsToSearch = listOf(
                                join.track?.song,
                                join.track?.artist,
                                join.track?.album,
                                join.show?.name
                            )

                            fieldsToSearch.forEach { field ->
                                field?.let {
                                    if (!filteredList.contains(join) &&
                                        "^$query".toRegex()
                                            .find(it.toLowerCase().removeLeadingArticles()) != null)
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
        val newResults = sortFavorites(results)

        newResults?.let {
            results.clear()
            results.addAll(it)
            notifyDataSetChanged()
        }
    }

    private fun sortFavorites(list: List<FavoriteJoin>?): List<FavoriteJoin>? {
        return (when (fragment.sortDirection) {
            SortDirection.ASC -> when (fragment.sortType) {
                SortType.RECENT -> list?.sortedBy{it.favorite?.favoriteId}
                SortType.ALBUM  -> list?.sortedBy{it.track?.album?.toUpperCase().removeLeadingArticles()}
                SortType.ARTIST -> list?.sortedBy{it.track?.artist?.toUpperCase().removeLeadingArticles()}
                SortType.TRACK  -> list?.sortedBy{it.track?.song?.toUpperCase().removeLeadingArticles()}
                SortType.SHOW   -> list?.sortedBy{it.show?.name?.toUpperCase().removeLeadingArticles()}
            }
            SortDirection.DES -> when (fragment.sortType) {
                SortType.RECENT -> list?.sortedByDescending{it.favorite?.favoriteId}
                SortType.ALBUM  -> list?.sortedByDescending{it.track?.album?.toUpperCase().removeLeadingArticles()}
                SortType.ARTIST -> list?.sortedByDescending{it.track?.artist?.toUpperCase().removeLeadingArticles()}
                SortType.TRACK  -> list?.sortedByDescending{it.track?.song?.toUpperCase().removeLeadingArticles()}
                SortType.SHOW   -> list?.sortedByDescending{it.show?.name?.toUpperCase().removeLeadingArticles()}
            }
        })
    }

    enum class SortDirection(val type: String) {
        ASC("asc"),
        DES("des")
    }

    enum class SortType {
        RECENT,
        SHOW,
        ARTIST,
        ALBUM,
        TRACK
    }

    class ViewHolder(
        private val binding: CellFavoriteTrackBinding,
        private val queryStr: String
    ) : BindingViewHolder<FavoriteJoin>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: FavoriteJoin) {
            binding.apply {
                clickListener = listener
                track = item.track
                show = item.show
                query = queryStr
            }
        }
    }
}
