package fho.kdvs.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import fho.kdvs.databinding.CellFavoriteTrackBinding
import fho.kdvs.global.database.*
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import timber.log.Timber

class FavoriteViewAdapter(
    private val joins: List<ShowBroadcastTrackFavoriteJoin>?,
    private val fragment: FavoriteFragment,
    onClick: (ClickData<ShowBroadcastTrackFavoriteJoin>) -> Unit
) : BindingRecyclerViewAdapter<Pair<FavoriteEntity?, TrackEntity?>, FavoriteViewAdapter.ViewHolder>(onClick, FavoriteTrackDiffCallback()), Filterable {

    var query: String = ""
    private var sortType = SortType.RECENT
    private var tracksFilteredAndSorted: List<Pair<FavoriteEntity?, TrackEntity?>>? = null

    private var show: ShowEntity? = null
    private var broadcasts: List<BroadcastEntity?>? = null
    private var tracks: List<TrackEntity?>? = null
    private var favorites: List<FavoriteEntity?>? = null
    private var favoriteTracks: List<Pair<FavoriteEntity?, TrackEntity?>>? = null

    init {
        show = joins
            ?.map { it.show }
            ?.first()
        broadcasts = joins
            ?.flatMap { it.getBroadcasts() }
        tracks = joins
            ?.flatMap { it.getTracks() }
        favorites = joins
            ?.flatMap { it.getFavorites()}

        val favoritedTracks = tracks
            ?.filter { (favorites
                ?.count { f -> f?.trackId == it?.trackId } ?: 0) > 0}
        favoriteTracks = favoritedTracks
            ?.map { val favorite = favorites?.first { f -> f.trackId == it.trackId }
                Pair(favorite, it) }

        tracksFilteredAndSorted = favoriteTracks
            ?.sortedBy { it.first.favoriteId }

        submitList(tracksFilteredAndSorted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellFavoriteTrackBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, query)
    }

    override fun getItemCount(): Int {
        return tracksFilteredAndSorted?.size ?: 0
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSeq: CharSequence): FilterResults {
                val filteredList = ArrayList<ShowBroadcastTrackFavoriteJoin>()
                val query = charSeq.toString().trim()

                if (query.isNotEmpty()){
                    if (fragment.hashedTracks[query] != null) {
                        filteredList.addAll(fragment.hashedTracks[query]!!)
                    } else {
                        favoriteTracks?.forEachIndexed {index, track ->
                            val metadataFieldsToSearch = listOf(
                                track.getTrack()?.song,
                                track.getTrack()?.artist,
                                track.getTrack()?.album)
                            metadataFieldsToSearch.forEach { field ->
                                field?.let {
                                    if ("^$query".toRegex()
                                            .find(removeArticles(it.toLowerCase())) != null)
                                        filteredList.add(track)
                                }
                            }
                        }
                        tracksFilteredAndSorted = filteredList
                        fragment.hashedTracks[query] = filteredList
                    }
                } else {
                    tracksFilteredAndSorted = mutableListOf()
                }

                val sortedList = sortList(filteredList)

                val filterResults = FilterResults()
                filterResults.values = sortedList
                filterResults.count = sortedList.size

                return filterResults
            }

            private fun sortList(filteredResults: List<Pair<FavoriteEntity?, TrackEntity?>>): List<ShowBroadcastTrackFavoriteJoin> {
                return when (sortType) {
                    SortType.RECENT -> filteredResults.sortedBy{it()?.favoriteId}
                    SortType.ALBUM  -> filteredResults.sortedBy{it.getTrack()?.album}
                    SortType.ARTIST -> filteredResults.sortedBy{it.getTrack()?.artist}
                    SortType.SONG   -> filteredResults.sortedBy{it.getTrack()?.song}
                    SortType.SHOW   -> filteredResults.sortedBy{it.show?.name}
                }
            }

            @SuppressWarnings("unchecked")
            override fun publishResults(charSeq: CharSequence, results: FilterResults) {
                Timber.d("${results.count} results found")
                if (results.values is List<*>) {
                    tracksFilteredAndSorted = results.values as? List<ShowBroadcastTrackFavoriteJoin>? // TODO: safe cast?

                    // alphabetical sort ignoring leading articles
                    submitList(tracksFilteredAndSorted)
                }
            }
        }
    }

    private fun removeArticles(str: String?): String {
        return """^(?:(the|a|an) +)""".toRegex()
            .replace(str ?: "", "")
    }

    class ViewHolder(
        private val binding: CellFavoriteTrackBinding,
        private val queryStr: String
    ) : BindingViewHolder<Pair<FavoriteEntity?, TrackEntity?>>(binding.root) {
        override fun bind(listener: View.OnClickListener, item:  Pair<FavoriteEntity?, TrackEntity?>) {
            binding.apply {
                clickListener = listener
                track = item.second
                query = queryStr
            }
        }
    }

    private enum class SortType {
        RECENT,
        SHOW,
        ARTIST,
        ALBUM,
        SONG
    }
}