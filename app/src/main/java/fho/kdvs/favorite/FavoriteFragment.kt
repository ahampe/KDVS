package fho.kdvs.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import fho.kdvs.R
import fho.kdvs.api.service.SpotifyService
import fho.kdvs.favorite.FavoriteFragment.SortDirection
import fho.kdvs.favorite.FavoriteFragment.SortType
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.preferences.KdvsPreferences
import kotlinx.android.synthetic.main.fragment_favorite.*
import javax.inject.Inject

const val POS_TRACKS = 0
const val POS_BROADCASTS = 1

/**
 * Fragment with a pager containing [FavoriteTrackFragment] and [FavoriteBroadcastFragment].
 * Handles a sort menu and search bar shared between the two pager fragments.
 * [SortDirection] and [SortType] states are communicated to child fragments via [SharedViewModel].
 */
class FavoriteFragment : BaseFragment() {

    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    @Inject
    lateinit var spotifyService: SpotifyService

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var favoritePagerAdapter: FavoritePagerAdapter

    private var currentPagePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        searchBar?.clearFocus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritePagerAdapter = FavoritePagerAdapter(requireFragmentManager())
        favoritePager.adapter = favoritePagerAdapter

        tabLayout.setupWithViewPager(favoritePager)

        favoritePager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                currentPagePosition = position

                hideSortMenu()

                when (position) {
                    POS_TRACKS -> initForTracks()
                    POS_BROADCASTS -> initForBroadcasts()
                }
            }
        })



        initializeClickListeners()
    }

    private fun initializeClickListeners() {
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

            layout.setOnClickListener {
                button?.visibility = View.VISIBLE

                sharedViewModel.favoriteSortType.postValue(pair.second)

                if (button?.tag == SortDirection.ASC.type) {
                    button.tag = SortDirection.DES.type
                    button.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
                    sharedViewModel.favoriteSortDirection.postValue(SortDirection.DES)

                } else if (button?.tag == SortDirection.DES.type) {
                    button.tag = SortDirection.ASC.type
                    button.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
                    sharedViewModel.favoriteSortDirection.postValue(SortDirection.ASC)
                }

                when (currentPagePosition) {
                    POS_TRACKS -> sortTracks()
                    POS_BROADCASTS -> sortBroadcasts()
                }

                val otherPairs = layoutToSortType.filter { p -> p != pair }
                otherPairs.forEach { p ->
                    val otherButton = p.first.getChildAt(1)
                    otherButton.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun sortTracks() {
        getFavoriteTrackViewAdapter()?.updateData()
    }

    private fun sortBroadcasts() {

    }

    private fun initForTracks() {
        initSearchBarForTracks()
        initSortMenuForTracks()
    }

    private fun initForBroadcasts() {
        initSearchBarForBroadcasts()
        initSortMenuForBroadcasts()
    }

    private fun initSearchBarForTracks() {
        val favoriteTrackViewAdapter = getFavoriteTrackViewAdapter()

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

    private fun initSearchBarForBroadcasts() { // TODO
        val favoriteViewAdapter = favoritePagerAdapter.favoriteTrackFrag.favoriteTrackViewAdapter

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

    /**
     * Broadcasts' sort options are [SortType.RECENT], [SortType.SHOW], [SortType.ARTIST],
     * [SortType.ALBUM], [SortType.TRACK]
     */
    private fun initSortMenuForTracks() {
        initSortMenu()

        sortDate.visibility = View.GONE
        sortArtist.visibility = View.VISIBLE
        sortAlbum.visibility = View.VISIBLE
        sortTrack.visibility = View.VISIBLE
    }

    /**
     * Broadcasts' sort options are [SortType.DATE], [SortType.RECENT], [SortType.SHOW]
     */
    private fun initSortMenuForBroadcasts() {
        initSortMenu()

        sortDate.visibility = View.VISIBLE
        sortArtist.visibility = View.GONE
        sortAlbum.visibility = View.GONE
        sortTrack.visibility = View.GONE
    }

    private fun initSortMenu() {
        // reset sortType / Dir to default
    }

    private fun hideSortMenu() {
        sortMenu.visibility = View.GONE
    }

    private fun getFavoriteTrackViewAdapter() =
        favoritePagerAdapter.favoriteTrackFrag.favoriteTrackViewAdapter

    enum class SortDirection(val type: String) {
        ASC("asc"),
        DES("des")
    }

    enum class SortType {
        RECENT,
        DATE,
        SHOW,
        ARTIST,
        ALBUM,
        TRACK
    }
}
