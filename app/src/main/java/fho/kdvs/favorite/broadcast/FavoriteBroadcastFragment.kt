package fho.kdvs.favorite.broadcast

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
import fho.kdvs.dialog.BinaryChoiceDialogFragment
import fho.kdvs.favorite.FavoriteFragment.SortDirection
import fho.kdvs.favorite.FavoriteFragment.SortType
import fho.kdvs.favorite.FavoritePage
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.ShowBroadcastFavoriteJoin
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.RequestCodes
import kotlinx.android.synthetic.main.cell_favorite_broadcast.view.*
import kotlinx.android.synthetic.main.favorite_page_sort_menu.*
import kotlinx.android.synthetic.main.favorite_page_top_controls.*
import kotlinx.android.synthetic.main.fragment_favorite_broadcast.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class FavoriteBroadcastFragment : BaseFragment(), FavoritePage<ShowBroadcastFavoriteJoin> {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    private lateinit var viewModel: FavoriteBroadcastViewModel
    private lateinit var sharedViewModel: SharedViewModel

    var favoriteBroadcastViewAdapter: FavoriteBroadcastViewAdapter? = null

    val hashedResults = mutableMapOf<String, ArrayList<FavoriteBroadcastJoin>>()
    val currentlyDisplayingResults = mutableListOf<FavoriteBroadcastJoin?>()

    var sortType = SortType.RECENT
    var sortDirection = SortDirection.DES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(FavoriteBroadcastViewModel::class.java)

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_broadcast, container, false)
    }

    override fun onResume() {
        super.onResume()

        favoriteBroadcastViewAdapter?.updateData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        searchBar?.clearFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCodes.DOWNLOAD_ALL_FAVORITES_DIALOG -> {
                if (resultCode == Activity.RESULT_OK) {
                    val folder = sharedViewModel.getDownloadFolder()

                    favoriteBroadcastViewAdapter?.allFavorites?.forEach { join ->
                        launch {
                            sharedViewModel.downloadBroadcast(
                                requireActivity(),
                                join.broadcast,
                                join.show,
                                folder
                            )
                        }
                    }
                }
            }
        }
    }

    override fun subscribeToViewModel() {
        val fragment = this

        viewModel.run {
            showBroadcastFavoriteJoins.observe(fragment, Observer { joins ->
                processFavorites(joins)
            })
        }
    }

    override fun processFavorites(joins: List<ShowBroadcastFavoriteJoin>?) {
        when (joins?.isEmpty()) {
            true -> {
                resultsRecycler.visibility = View.GONE
                noResults.visibility = View.VISIBLE
            }
            false -> {
                resultsRecycler.visibility = View.VISIBLE
                noResults.visibility = View.GONE

                favoriteBroadcastViewAdapter =
                    FavoriteBroadcastViewAdapter(joins.distinct(), this) {
                        Timber.d("clicked ${it.item}")

                        viewModel.onClickBroadcast(findNavController(), it.item.broadcast)
                    }

                resultsRecycler.run {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = favoriteBroadcastViewAdapter
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
                    resultsRecycler.findViewHolderForAdapterPosition(i) as? FavoriteBroadcastViewAdapter.ViewHolder
                val key = when (sortType) {
                    SortType.SHOW -> holder?.itemView?.showName?.text
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
        downloadAllButton?.setOnClickListener {
            displayDialog()
        }

        val layoutToSortType = listOf(
            Pair(sortRecent, SortType.RECENT),
            Pair(sortShow, SortType.SHOW),
            Pair(sortDate, SortType.DATE)
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

                favoriteBroadcastViewAdapter?.updateData()

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
                    favoriteBroadcastViewAdapter?.filter?.filter(query)
                    favoriteBroadcastViewAdapter?.query = query
                    searchBar.clearFocus()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    favoriteBroadcastViewAdapter?.filter?.filter(query)
                    favoriteBroadcastViewAdapter?.query = query
                    return false
                }
            })

            // Display all results upon closing filter
            setOnCloseListener {
                favoriteBroadcastViewAdapter?.let {
                    it.results.clear()
                    it.results.addAll(it.allFavorites)
                    it.updateData()
                    searchBar.clearFocus()
                }

                true
            }
        }
    }

    private fun displayDialog() {
        val dialog = BinaryChoiceDialogFragment()
        val args = Bundle()

        args.putString("title", "Download")
        args.putString("message", "Download all favorited broadcasts?")

        dialog.arguments = args
        dialog.setTargetFragment(
            this@FavoriteBroadcastFragment,
            RequestCodes.DOWNLOAD_ALL_FAVORITES_DIALOG
        )
        dialog.show(requireFragmentManager(), "tag")
    }
}
