package fho.kdvs.favorite

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
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.ui.LoadScreen
import kotlinx.android.synthetic.main.cell_favorite_track.view.*
import kotlinx.android.synthetic.main.fragment_favorite.*
import timber.log.Timber
import javax.inject.Inject

@kotlinx.serialization.UnstableDefault
class FavoriteFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: FavoriteViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private var favoriteViewAdapter: FavoriteViewAdapter? = null
    
    val hashedResults = mutableMapOf<String, ArrayList<FavoriteJoin>>()
    val resultIds = mutableListOf<Int?>()

    var sortType = FavoriteViewAdapter.SortType.RECENT
    var sortDirection = FavoriteViewAdapter.SortDirection.DES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(FavoriteViewModel::class.java)

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        searchBar?.clearFocus()
    }

    override fun onResume() {
        super.onResume()

        favoriteViewAdapter?.updateData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(favoritesRoot)

        initializeClickListeners()
        initializeSearchBar()
    }

    /** Separate name-based results by alphabetical character headers. */
    private fun setSectionHeaders() {
        if (resultsRecycler == null) return

        val headers = mutableListOf<String>()

        clearSectionHeaders()

        if (sortType != FavoriteViewAdapter.SortType.RECENT) {
            for (i in 0..resultsRecycler.childCount) {
                val holder = resultsRecycler.findViewHolderForAdapterPosition(i)
                val key = when(sortType) {
                    FavoriteViewAdapter.SortType.SHOW   -> holder?.itemView?.showName?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    FavoriteViewAdapter.SortType.ARTIST -> holder?.itemView?.trackInfo?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.split(getString(R.string.track_info_separator))
                        ?.firstOrNull()
                        ?.trim()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    FavoriteViewAdapter.SortType.ALBUM  -> holder?.itemView?.trackInfo?.text
                        ?.toString()
                        ?.removeLeadingArticles()
                        ?.split(getString(R.string.track_info_separator))
                        ?.getOrNull(1)
                        ?.trim()
                        ?.firstOrNull()
                        ?.toUpperCase()
                        ?.toString()
                    FavoriteViewAdapter.SortType.TRACK  -> holder?.itemView?.song?.text
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

    private fun subscribeToViewModel(){
        val fragment = this

        viewModel.run {
            getShowBroadcastTrackFavoriteJoins().observe(fragment, Observer { joins ->
                when (joins.isEmpty()) {
                    true -> {
                        resultsRecycler.visibility = View.GONE
                        noResults.visibility = View.VISIBLE

                        LoadScreen.hideLoadScreen(favoritesRoot)
                    }
                    false -> {
                        resultsRecycler.visibility = View.VISIBLE
                        noResults.visibility = View.GONE

                        favoriteViewAdapter = FavoriteViewAdapter(joins, fragment) {
                            Timber.d("clicked ${it.item}")

                            val array = resultIds
                                .filterNotNull()
                                .toIntArray()

                            viewModel.onClickTrack(findNavController(), it.item.track, array)
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

                        LoadScreen.hideLoadScreen(favoritesRoot)
                    }
                }
            })
        }
    }

    private fun initializeClickListeners(){
        val layoutToSortType = listOf(
            Pair(recent, FavoriteViewAdapter.SortType.RECENT),
            Pair(show,   FavoriteViewAdapter.SortType.SHOW),
            Pair(artist, FavoriteViewAdapter.SortType.ARTIST),
            Pair(album,  FavoriteViewAdapter.SortType.ALBUM),
            Pair(track,  FavoriteViewAdapter.SortType.TRACK)
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

                if (button?.tag == FavoriteViewAdapter.SortDirection.ASC.type) {
                    button.tag = FavoriteViewAdapter.SortDirection.DES.type
                    button.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
                    sortDirection = FavoriteViewAdapter.SortDirection.DES
                    favoriteViewAdapter?.updateData()
                } else if (button?.tag == FavoriteViewAdapter.SortDirection.DES.type) {
                    button.tag = FavoriteViewAdapter.SortDirection.ASC.type
                    button.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
                    sortDirection = FavoriteViewAdapter.SortDirection.ASC
                    favoriteViewAdapter?.updateData()
                }

                val otherPairs = layoutToSortType.filter { p -> p != pair }
                otherPairs.forEach {p ->
                    val otherButton = p.first.getChildAt(1)
                    otherButton.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun initializeSearchBar(){
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
}
