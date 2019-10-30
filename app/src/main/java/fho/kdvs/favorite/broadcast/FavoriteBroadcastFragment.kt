package fho.kdvs.favorite.broadcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.R
import fho.kdvs.favorite.FavoriteFragment.SortType
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.ShowBroadcastFavoriteJoin
import fho.kdvs.global.extensions.removeLeadingArticles
import fho.kdvs.global.preferences.KdvsPreferences
import kotlinx.android.synthetic.main.cell_favorite_broadcast.view.*
import kotlinx.android.synthetic.main.fragment_favorite_broadcast.*
import timber.log.Timber
import javax.inject.Inject


class FavoriteBroadcastFragment : BaseFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    private lateinit var viewModel: FavoriteBroadcastViewModel
    private lateinit var sharedViewModel: SharedViewModel

    var favoriteBroadcastViewAdapter: FavoriteBroadcastViewAdapter? = null

    val hashedResults = mutableMapOf<String, ArrayList<FavoriteBroadcastJoin>>()
    val currentlyDisplayingResults = mutableListOf<FavoriteBroadcastJoin?>()

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

        //LoadScreen.displayLoadScreen(favoritesRoot)

        downloadAllButton?.setOnClickListener {
            // TODO
        }
    }

    /** Separate name-based results by alphabetical character headers. */
    private fun setSectionHeaders() {
        if (resultsRecycler == null) return

        val headers = mutableListOf<String>()

        clearSectionHeaders()

        sharedViewModel.favoriteSortType.observe(this, Observer { sortType ->
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
        })
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
            showBroadcastFavoriteJoins.observe(fragment, Observer { joins ->
                processBroadcastFavorites(joins)

                //LoadScreen.hideLoadScreen(favoritesRoot)
            })
        }
    }

    private fun processBroadcastFavorites(joins: List<ShowBroadcastFavoriteJoin>?) {
        when (joins?.isEmpty()) {
            true -> {
                resultsRecycler.visibility = View.GONE
                downloadAllButton.visibility = View.GONE
                noResults.visibility = View.VISIBLE

                //LoadScreen.hideLoadScreen(favoritesRoot)
            }
            false -> {
                resultsRecycler.visibility = View.VISIBLE
                noResults.visibility = View.GONE

                favoriteBroadcastViewAdapter =
                    FavoriteBroadcastViewAdapter(joins.distinct(), this, sharedViewModel) {
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
}
