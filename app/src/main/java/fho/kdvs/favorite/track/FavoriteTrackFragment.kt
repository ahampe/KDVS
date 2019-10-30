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
import androidx.viewpager.widget.ViewPager
import fho.kdvs.R
import fho.kdvs.api.service.SpotifyService
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.ShowBroadcastFavoriteJoin
import fho.kdvs.global.database.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.enums.ThirdPartyService
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.ExportManagerSpotify
import fho.kdvs.global.util.RequestCodes
import kotlinx.android.synthetic.main.cell_favorite_track.view.*
import kotlinx.android.synthetic.main.fragment_favorite_track.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FavoriteTrackFragment : BaseFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    @Inject
    lateinit var spotifyService: SpotifyService

    private lateinit var viewModel: FavoriteTrackViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var viewPager: ViewPager

    private var favoriteViewAdapter: FavoriteTrackViewAdapter? = null

    val hashedResults = mutableMapOf<String, ArrayList<FavoriteTrackJoin>>()
    val currentlyDisplayingResults = mutableListOf<FavoriteTrackJoin?>()

    var sortType = FavoriteTrackViewAdapter.SortType.RECENT
    var sortDirection = FavoriteTrackViewAdapter.SortDirection.DES

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

    override fun onDestroyView() {
        super.onDestroyView()

        //searchBar?.clearFocus()
    }

    override fun onResume() {
        super.onResume()

        favoriteViewAdapter?.updateData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //LoadScreen.displayLoadScreen(favoritesRoot)

        initializeClickListeners()
        initializeSearchBar()
        initializeIcons()
    }

    /** Handle third-party getExportPlaylistUri request. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCodes.SPOTIFY_EXPORT_FAVORITES -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (sharedViewModel.isSpotifyAuthVoidOrExpired()) {
                        sharedViewModel.loginSpotify(requireActivity())
                        sharedViewModel.spotToken.observe(viewLifecycleOwner, Observer { token ->
                            token?.let {
                                exportTracksToSpotify(kdvsPreferences.spotifyAuthToken as String)
                            }
                        })
                    } else {
                        exportTracksToSpotify(kdvsPreferences.spotifyAuthToken as String)
                    }
                }
            }
            RequestCodes.YOUTUBE_EXPORT_FAVORITES -> {
                exportTracksToYouTube()
            }
        }
    }

    /** Separate name-based results by alphabetical character headers. */
    private fun setSectionHeaders() {
        if (resultsRecycler == null) return

        val headers = mutableListOf<String>()

        clearSectionHeaders()

        if (sortType != FavoriteTrackViewAdapter.SortType.RECENT) {
            for (i in 0..resultsRecycler.childCount) {
                val holder =
                    resultsRecycler.findViewHolderForAdapterPosition(i) as? FavoriteTrackViewAdapter.ViewHolder
                val key = when (sortType) {
                    FavoriteTrackViewAdapter.SortType.SHOW -> holder?.itemView?.trackInfo?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    FavoriteTrackViewAdapter.SortType.ARTIST -> holder?.itemView?.trackInfo?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.split(getString(R.string.track_info_separator))
                        ?.firstOrNull()
                        ?.trim()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    FavoriteTrackViewAdapter.SortType.ALBUM -> holder?.itemView?.trackInfo?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.split(getString(R.string.track_info_separator))
                        ?.getOrNull(1)
                        ?.trim()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    FavoriteTrackViewAdapter.SortType.TRACK -> holder?.itemView?.song?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    else -> return
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

    private fun clearSectionHeaders() {
        for (i in 0..resultsRecycler.childCount) {
            val holder = resultsRecycler.findViewHolderForAdapterPosition(i)
            holder?.itemView?.sectionHeader?.visibility = View.GONE
        }
    }

    private fun subscribeToViewModel() {
        val fragment = this

        viewModel.run {
            allJoins.observe(fragment, Observer { (broadcastFavoriteJoins, trackFavoriteJoins) ->
                processBroadcastFavorites(broadcastFavoriteJoins)

                processTrackFavorites(trackFavoriteJoins)

                //LoadScreen.hideLoadScreen(favoritesRoot)
            })
        }
    }

    private fun processBroadcastFavorites(joins: List<ShowBroadcastFavoriteJoin>?) {

    }

    private fun processTrackFavorites(joins: List<ShowBroadcastTrackFavoriteJoin>?) {
        when (joins?.isEmpty()) {
            true -> {
                resultsRecycler.visibility = View.GONE
                noResults.visibility = View.VISIBLE

                //LoadScreen.hideLoadScreen(favoritesRoot)
            }
            false -> {
                resultsRecycler.visibility = View.VISIBLE
                noResults.visibility = View.GONE

                favoriteViewAdapter =
                    FavoriteTrackViewAdapter(joins.distinct(), this) {
                        Timber.d("clicked ${it.item}")

                        val ids = currentlyDisplayingResults
                            .mapNotNull { r -> r?.track?.trackId }
                            .toIntArray()

                        viewModel.onClickTrack(findNavController(), it.item.track, ids)
                    }

                resultsRecycler.run {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = favoriteViewAdapter
                }

                if (resultsRecycler.viewTreeObserver.isAlive) {
                    resultsRecycler.viewTreeObserver.addOnDrawListener {
                        setSectionHeaders()
                    }
                }
            }
        }
    }

    private fun initializeClickListeners() {
        val layoutToSortType = listOf(
            Pair(recent, FavoriteTrackViewAdapter.SortType.RECENT),
            Pair(show, FavoriteTrackViewAdapter.SortType.SHOW),
            Pair(artist, FavoriteTrackViewAdapter.SortType.ARTIST),
            Pair(album, FavoriteTrackViewAdapter.SortType.ALBUM),
            Pair(track, FavoriteTrackViewAdapter.SortType.TRACK)
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

            layout.setOnClickListener {
                button?.visibility = View.VISIBLE
                sortType = pair.second

                if (button?.tag == FavoriteTrackViewAdapter.SortDirection.ASC.type) {
                    button.tag = FavoriteTrackViewAdapter.SortDirection.DES.type
                    button.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
                    sortDirection = FavoriteTrackViewAdapter.SortDirection.DES
                    favoriteViewAdapter?.updateData()
                } else if (button?.tag == FavoriteTrackViewAdapter.SortDirection.DES.type) {
                    button.tag = FavoriteTrackViewAdapter.SortDirection.ASC.type
                    button.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
                    sortDirection = FavoriteTrackViewAdapter.SortDirection.ASC
                    favoriteViewAdapter?.updateData()
                }

                val otherPairs = layoutToSortType.filter { p -> p != pair }
                otherPairs.forEach { p ->
                    val otherButton = p.first.getChildAt(1)
                    otherButton.visibility = View.INVISIBLE
                }
            }
        }

        spotifyExportIconFavorite?.setOnClickListener {
            sharedViewModel.onClickExportIcon(
                this,
                RequestCodes.SPOTIFY_EXPORT_FAVORITES,
                ThirdPartyService.SPOTIFY,
                currentlyDisplayingResults.count { r -> !r?.track?.spotifyTrackUri.isNullOrEmpty() }
            )
        }

        youtubeExportIconFavorite?.setOnClickListener {
            sharedViewModel.onClickExportIcon(
                this,
                RequestCodes.YOUTUBE_EXPORT_FAVORITES,
                ThirdPartyService.YOUTUBE,
                currentlyDisplayingResults.count { r -> !r?.track?.youTubeId.isNullOrEmpty() }
            )
        }
    }

    private fun initializeSearchBar() {
        searchBar?.run {
            queryHint = resources.getString(R.string.filter_query_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String): Boolean {
                    favoriteViewAdapter?.filter?.filter(query)
                    favoriteViewAdapter?.query = query
                    searchBar.clearFocus()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    favoriteViewAdapter?.filter?.filter(query)
                    favoriteViewAdapter?.query = query
                    return false
                }
            })

            // Display all results upon closing filter
            setOnCloseListener {
                favoriteViewAdapter?.let {
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

                view.setOnClickListener {
                    val visibleTracksSpotifyUris = currentlyDisplayingResults
                        .mapNotNull { r -> r?.track?.spotifyTrackUri }

                    val count = visibleTracksSpotifyUris.count()

                    sharedViewModel.onClickExportIcon(
                        this,
                        RequestCodes.SPOTIFY_EXPORT_FAVORITES,
                        ThirdPartyService.SPOTIFY,
                        count
                    )
                }
            }
        }
    }

    private fun exportTracksToSpotify(token: String) {
        val uris = currentlyDisplayingResults
            .mapNotNull { r -> r?.track?.spotifyTrackUri }

        launch {
            ExportManagerSpotify(
                context = requireContext(),
                spotifyService = spotifyService,
                trackUris = uris,
                userToken = token,
                playlistTitle = "My KDVS Favorites",
                storedPlaylistUri = kdvsPreferences.spotifyFavoritesPlaylistUri
            ).getExportPlaylistUri()
                ?.let {
                    kdvsPreferences.spotifyFavoritesPlaylistUri = it

                    sharedViewModel.openSpotify(requireContext(), it)
                }
        }
    }

    private fun exportTracksToYouTube() {
        val ids = currentlyDisplayingResults
            .mapNotNull { r -> r?.track?.youTubeId }

        sharedViewModel.exportVideosToYouTubePlaylist(requireContext(), ids)
    }
}
