package fho.kdvs.favorite.track

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.R
import fho.kdvs.api.service.SpotifyService
import fho.kdvs.dialog.ExportLoadingDialog
import fho.kdvs.favorite.FavoriteFragment.SortDirection
import fho.kdvs.favorite.FavoriteFragment.SortType
import fho.kdvs.favorite.FavoritePage
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.joins.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.enums.ThirdPartyService
import fho.kdvs.global.export.SpotifyExportManager
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.RequestCodes
import kotlinx.android.synthetic.main.cell_favorite_track.view.*
import kotlinx.android.synthetic.main.favorite_page_sort_menu.*
import kotlinx.android.synthetic.main.favorite_page_top_controls.*
import kotlinx.android.synthetic.main.fragment_favorite_track.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class FavoriteTrackFragment : BaseFragment(), FavoritePage<ShowBroadcastTrackFavoriteJoin> {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    @Inject
    lateinit var spotifyService: SpotifyService

    private lateinit var viewModel: FavoriteTrackViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var spotifyExportManager: SpotifyExportManager

    var favoriteTrackViewAdapter: FavoriteTrackViewAdapter? = null

    val hashedResults = mutableMapOf<String, ArrayList<FavoriteTrackJoin>>()
    val currentlyDisplayingResults = mutableListOf<FavoriteTrackJoin?>()

    var sortType = SortType.RECENT
    var sortDirection = SortDirection.DES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(FavoriteTrackViewModel::class.java)

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_track, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spotifyExportManager = SpotifyExportManager(
            requireActivity(),
            spotifyService,
            kdvsPreferences,
            sharedViewModel
        )

        initializeClickListeners()
        initializeSearchBar()
        initializeIcons()
    }

    /** Handle third-party export request launched in [FavoriteTrackFragment]. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCodes.SPOTIFY_EXPORT_FAVORITES -> {
                if (resultCode == Activity.RESULT_OK) {
                    spotifyExportManager.loginIfNecessary()
                    sharedViewModel.spotifyAuthToken.observe(
                        viewLifecycleOwner,
                        Observer { token ->
                            token?.let {
                                exportAllFavoritedTracksToSpotify()
                            }
                        })
                }
            }
            RequestCodes.YOUTUBE_EXPORT_FAVORITES -> {
                exportAllFavoritedTracksToYouTube()
            }
        }
    }

    override fun subscribeToViewModel() {
        val fragment = this

        viewModel.run {
            showBroadcastTrackFavoriteJoinsLiveData.observe(fragment, Observer { joins ->
                processFavorites(joins)
            })
        }
    }

    override fun processFavorites(joins: List<ShowBroadcastTrackFavoriteJoin>?) {
        if (joins == null) return

        when (joins.isEmpty()) {
            true -> {
                resultsRecycler.visibility = View.GONE
                noResults.visibility = View.VISIBLE
            }
            false -> {
                resultsRecycler.visibility = View.VISIBLE
                noResults.visibility = View.GONE

                favoriteTrackViewAdapter =
                    FavoriteTrackViewAdapter(joins.distinct(), this) {
                        Timber.d("clicked ${it.item}")

                        val ids = currentlyDisplayingResults
                            .mapNotNull { r -> r?.track?.trackId }
                            .toIntArray()

                        viewModel.onClickTrack(findNavController(), it.item.track, ids)
                    }

                resultsRecycler.run {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = favoriteTrackViewAdapter
                }

                if (resultsRecycler.viewTreeObserver.isAlive) {
                    resultsRecycler.viewTreeObserver.addOnDrawListener {
                        setSectionHeaders()
                    }
                }
            }
        }
    }

    /** Separate name-based results by alphabetical character headers. */
    override fun setSectionHeaders() {
        if (resultsRecycler == null) return

        val headers = mutableListOf<String>()

        clearSectionHeaders()

        if (sortType != SortType.RECENT) {
            for (i in 0..resultsRecycler.childCount) {
                val holder =
                    resultsRecycler.findViewHolderForAdapterPosition(i) as? FavoriteTrackViewAdapter.ViewHolder
                val key = when (sortType) {
                    SortType.SHOW -> holder?.itemView?.showName?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    SortType.ARTIST -> holder?.itemView?.trackInfo?.text
                        ?.toString()
                        ?.split(getString(R.string.track_info_separator))
                        ?.firstOrNull()
                        ?.trim()
                        ?.removeLeadingArticles()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    SortType.ALBUM -> holder?.itemView?.trackInfo?.text
                        ?.toString()
                        ?.split(getString(R.string.track_info_separator))
                        ?.getOrNull(1)
                        ?.trim()
                        ?.removeLeadingArticles()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    SortType.TRACK -> holder?.itemView?.song?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    else -> null
                }

                key?.let {
                    if (!headers.contains(key)) {
                        holder?.itemView?.sectionHeader?.text = key
                        holder?.itemView?.sectionHeader?.visibility = View.VISIBLE
                        headers.add(key)
                    } else {
                        holder?.itemView?.sectionHeader?.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun clearSectionHeaders() {
        for (i in 0..resultsRecycler.childCount) {
            val holder = resultsRecycler.findViewHolderForAdapterPosition(i)
            holder?.itemView?.sectionHeader?.visibility = View.GONE
        }
    }

    override fun initializeClickListeners() {
        spotifyExportIconFavorite?.setOnClickListener {
            sharedViewModel.onClickExportIcon(
                this,
                RequestCodes.SPOTIFY_EXPORT_FAVORITES,
                ThirdPartyService.SPOTIFY
            )
        }

        youtubeExportIconFavorite?.setOnClickListener {
            sharedViewModel.onClickExportIcon(
                this,
                RequestCodes.YOUTUBE_EXPORT_FAVORITES,
                ThirdPartyService.YOUTUBE
            )
        }

        val layoutToSortType = listOf(
            Pair(sortRecent, SortType.RECENT),
            Pair(sortShow, SortType.SHOW),
            Pair(sortArtist, SortType.ARTIST),
            Pair(sortAlbum, SortType.ALBUM),
            Pair(sortTrack, SortType.TRACK)
        )

        dummy.setOnClickListener {
            sortMenu.visibility = View.GONE
            dummy.visibility = View.GONE
        }

        filter.setOnClickListener {
            sortMenu.visibility = if (sortMenu.visibility == View.GONE)
                View.VISIBLE else View.GONE
            dummy.visibility = if (sortMenu.visibility == View.VISIBLE)
                View.VISIBLE else View.GONE
        }

        layoutToSortType.forEach { pair ->
            val layout = pair.first
            val button = layout.getChildAt(1) as? ImageView

            layout.visibility = View.VISIBLE

            layout.setOnClickListener {
                button?.visibility = View.VISIBLE

                sortType = pair.second

                if (button?.tag == SortDirection.ASC.type) {
                    button.tag = SortDirection.DES.type
                    button.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
                    sortDirection = SortDirection.DES

                } else if (button?.tag == SortDirection.DES.type) {
                    button.tag = SortDirection.ASC.type
                    button.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
                    sortDirection = SortDirection.ASC
                }

                favoriteTrackViewAdapter?.updateData()

                val otherPairs = layoutToSortType.filter { p -> p != pair }
                otherPairs.forEach { p ->
                    val otherButton = p.first.getChildAt(1)
                    otherButton.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun initializeSearchBar() {
        searchBar?.run {
            queryHint = resources.getString(R.string.filter_query_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String): Boolean {
                    favoriteTrackViewAdapter?.filter?.filter(query)
                    favoriteTrackViewAdapter?.query = query
                    searchBar.clearFocus()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    favoriteTrackViewAdapter?.filter?.filter(query)
                    favoriteTrackViewAdapter?.query = query
                    return false
                }
            })

            // Display all results upon closing filter
            setOnCloseListener {
                favoriteTrackViewAdapter?.let {
                    it.results.clear()
                    it.results.addAll(it.allFavorites)
                    it.updateData()
                    searchBar.clearFocus()
                }

                true
            }
        }
    }

    private fun initializeIcons() {
        spotifyExportIconFavorite?.let { view ->
            if (sharedViewModel.isSpotifyInstalledOnDevice(view.context)) {
                view.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Note: we can assume all third-party data fetches have launched for favorited tracks, but
     * we can't assume that those fetches have all completed by the time the user navigates to
     * [FavoriteTrackFragment] and clicks export.
     */
    private fun exportAllFavoritedTracksToSpotify() {
        var hasExecuted = false
        var exportJob: Job? = null

        val loadingDialog =
            ExportLoadingDialog(requireContext()) {
                exportJob?.cancel()
            }.apply {
                display()
            }

        viewModel.favoritedTracksLiveData.observe(this, Observer { tracks ->
            if (!hasExecuted && tracks.all { t -> t.hasThirdPartyInfo }) {
                val uris = tracks.mapNotNull { t -> t.spotifyTrackUri }

                exportJob = launch {
                    spotifyExportManager.exportTracksToStoredPlaylistAsync(
                        uris,
                        kdvsPreferences.spotifyFavoritesPlaylistUri
                    ).await()?.let {
                        kdvsPreferences.spotifyFavoritesPlaylistUri = it

                        sharedViewModel.openSpotify(requireContext(), it)

                        loadingDialog.hide()
                    }
                }

                hasExecuted = true
            }
        })
    }

    private fun exportAllFavoritedTracksToYouTube() {
        var hasExecuted = false

        val loadingDialog =
            ExportLoadingDialog(requireContext()) {
                hasExecuted = true
            }.apply {
                display()
            }

        viewModel.favoritedTracksLiveData.observe(this, Observer { tracks ->
            if (!hasExecuted && tracks.all { t -> t.hasThirdPartyInfo }) {
                val ids = tracks.mapNotNull { t -> t.youTubeId }

                sharedViewModel.exportVideosToYouTubePlaylist(requireContext(), ids)

                loadingDialog.hide()

                hasExecuted = true
            }
        })
    }
}
