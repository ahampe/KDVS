package fho.kdvs.favorite.track

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellFavoriteTrackBinding
import fho.kdvs.favorite.FavoriteFragment.SortDirection
import fho.kdvs.favorite.FavoriteFragment.SortType
import fho.kdvs.global.database.joins.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.database.joins.getTrackFavoriteJoins
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import timber.log.Timber


class FavoriteTrackViewAdapter(
    trackJoins: List<ShowBroadcastTrackFavoriteJoin>?,
    private val fragment: FavoriteTrackFragment,
    onClick: (ClickData<FavoriteTrackJoin>) -> Unit
) : BindingRecyclerViewAdapter<FavoriteTrackJoin, FavoriteTrackViewAdapter.ViewHolder>(
    onClick,
    FavoriteTrackDiffCallback()
), Filterable {
    var query: String = ""
    val results = mutableListOf<FavoriteTrackJoin>()
    var allFavorites = mutableListOf<FavoriteTrackJoin>()

    init {
        val trackFavoriteJoins = trackJoins.getTrackFavoriteJoins()

        trackFavoriteJoins?.let {
            results.addAll(it)
        }

        allFavorites.addAll(results)

        updateFragmentResults()

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
                val filteredList = ArrayList<FavoriteTrackJoin>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()) {
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
                SortType.RECENT -> results.sortedBy { it.favorite?.favoriteTrackId }
                SortType.ALBUM -> results.sortedBy {
                    it.track?.album?.formatName()
                }
                SortType.ARTIST -> results.sortedBy {
                    it.track?.artist?.formatName()
                }
                SortType.TRACK -> results.sortedBy {
                    it.track?.song?.formatName()
                }
                SortType.SHOW -> results.sortedBy {
                    it.show?.name?.formatName()
                }
                else -> results
            }
            SortDirection.DES -> when (sortType) {
                SortType.RECENT -> results.sortedByDescending { it.favorite?.favoriteTrackId }
                SortType.ALBUM -> results.sortedByDescending {
                    it.track?.album?.formatName()
                }
                SortType.ARTIST -> results.sortedByDescending {
                    it.track?.artist?.formatName()
                }
                SortType.TRACK -> results.sortedByDescending {
                    it.track?.song?.formatName()
                }
                SortType.SHOW -> results.sortedByDescending {
                    it.show?.name?.formatName()
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
        private val binding: CellFavoriteTrackBinding,
        private val queryStr: String
    ) : BindingViewHolder<FavoriteTrackJoin>(binding.root) {
        override fun bind(listener: View.OnClickListener, item: FavoriteTrackJoin) {
            binding.apply {
                clickListener = listener
                track = item.track
                show = item.show
                query = queryStr
            }
        }
    }
}
