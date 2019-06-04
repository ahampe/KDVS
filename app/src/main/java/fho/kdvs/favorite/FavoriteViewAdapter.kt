package fho.kdvs.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellFavoriteTrackBinding
import fho.kdvs.global.database.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.database.getBroadcasts
import fho.kdvs.global.database.getFavorites
import fho.kdvs.global.database.getTracks
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import timber.log.Timber

class FavoriteViewAdapter(
    private val joins: List<ShowBroadcastTrackFavoriteJoin>?,
    private val fragment: FavoriteFragment,
    onClick: (ClickData<FavoriteJoin>) -> Unit
) : BindingRecyclerViewAdapter<FavoriteJoin, FavoriteViewAdapter.ViewHolder>(onClick, FavoriteTrackDiffCallback()), Filterable {

    var query: String = ""

    private val favoriteJoins = mutableListOf<FavoriteJoin>()
    private var favoriteJoinsFiltered: List<FavoriteJoin>? = null
    
    init {
        val shows = joins
            ?.map { it.show }
            ?.distinct()
        val broadcasts = joins
            ?.flatMap { it.getBroadcasts() }
            ?.distinct()
        val tracks = joins
            ?.flatMap { it.getTracks() }
            ?.distinct()
        val favorites = joins
            ?.flatMap { it.getFavorites()}
            ?.distinct()
        
        favorites?.forEach { favorite ->
            val track = tracks
                ?.firstOrNull{ 
                    it?.trackId == favorite?.trackId 
                }
            val broadcast = broadcasts
                ?.firstOrNull {
                    it?.broadcastId == track?.broadcastId
                }
            val show = shows
                ?.firstOrNull { 
                    it?.id == broadcast?.showId
                }
            
            favoriteJoins.add(FavoriteJoin(favorite, track, broadcast, show))
        }

        favoriteJoinsFiltered = favoriteJoins
            .sortedBy { it.favorite?.favoriteId }
        
        submitList(favoriteJoinsFiltered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellFavoriteTrackBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, query)
    }

    override fun getItemCount(): Int {
        return favoriteJoinsFiltered?.size ?: 0
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<FavoriteJoin>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()){
                    if (!fragment.hashedResults[query].isNullOrEmpty()) {
                        filteredList.addAll(fragment.hashedResults[query]!!)
                    } else {
                        favoriteJoins.forEach { join ->
                            val metadataFieldsToSearch = listOf(
                                join.track?.song,
                                join.track?.artist,
                                join.track?.album
                            )

                            metadataFieldsToSearch.forEach { field ->
                                field?.let {
                                    if (!filteredList.contains(join) &&
                                        "^$query".toRegex()
                                            .find(removeArticles(it.toLowerCase())) != null)
                                        filteredList.add(join)
                                }
                            }
                        }
                        favoriteJoinsFiltered = filteredList
                        fragment.hashedResults[query] = filteredList
                    }
                } else {
                    favoriteJoinsFiltered = mutableListOf()
                }

                sortList(SortDirection.ASC)

                val filterResults = FilterResults()
                filterResults.values = favoriteJoinsFiltered
                filterResults.count = favoriteJoinsFiltered?.size ?: 0

                return filterResults
            }

            @SuppressWarnings("unchecked")
            override fun publishResults(charSeq: CharSequence, results: FilterResults) {
                Timber.d("${results.count} results found")
                if (results.values is List<*>) {
                    //favoriteJoinsFiltered = results.values as? List<FavoriteJoin>? // TODO: safe cast?

                    // alphabetical sort ignoring leading articles
                    submitList(favoriteJoinsFiltered)
                }
            }
        }
    }

    private fun removeArticles(str: String?): String {
        return """^(?:(the|a|an) +)""".toRegex()
            .replace(str ?: "", "") // TODO: make extension
    }

    fun sortList(direction: SortDirection) {
        submitList(when (direction) {
            SortDirection.ASC -> when (fragment.sortType) {
                SortType.RECENT -> favoriteJoinsFiltered?.sortedBy{it.favorite?.favoriteId}
                SortType.ALBUM  -> favoriteJoinsFiltered?.sortedBy{it.track?.album}
                SortType.ARTIST -> favoriteJoinsFiltered?.sortedBy{it.track?.artist}
                SortType.TRACK  -> favoriteJoinsFiltered?.sortedBy{it.track?.song}
                SortType.SHOW   -> favoriteJoinsFiltered?.sortedBy{it.show?.name}
            }
            SortDirection.DES -> when (fragment.sortType) {
                SortType.RECENT -> favoriteJoinsFiltered?.sortedByDescending{it.favorite?.favoriteId}
                SortType.ALBUM  -> favoriteJoinsFiltered?.sortedByDescending{it.track?.album}
                SortType.ARTIST -> favoriteJoinsFiltered?.sortedByDescending{it.track?.artist}
                SortType.TRACK  -> favoriteJoinsFiltered?.sortedByDescending{it.track?.song}
                SortType.SHOW   -> favoriteJoinsFiltered?.sortedByDescending{it.show?.name}
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
        override fun bind(listener: View.OnClickListener, item:  FavoriteJoin) {
            binding.apply {
                clickListener = listener
                track = item.track
                query = queryStr
            }
        }
    }
}