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
import kotlinx.android.synthetic.main.fragment_favorites.*
import timber.log.Timber
import javax.inject.Inject

class FavoriteFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: FavoriteViewModel

    private var favoriteViewAdapter: FavoriteViewAdapter? = null
    
    var hashedResults = mutableMapOf<String, ArrayList<FavoriteJoin>>()
    var sortType = FavoriteViewAdapter.SortType.RECENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(FavoriteViewModel::class.java)

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeClickListeners()
        initializeSearchBar()
    }

    private fun subscribeToViewModel(){
        val fragment = this

        viewModel.run {
            getShowBroadcastTrackFavoriteJoins().observe(fragment, Observer { joins ->
                favoriteViewAdapter = FavoriteViewAdapter(joins, fragment) {
                    Timber.d("clicked ${it.item}")
                    viewModel.onClickTrack(findNavController(), it.item.track)
                }

                resultsRecycler.run {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = favoriteViewAdapter
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
            dummy.visibility = if (sortMenu.visibility == View.GONE)
                View.VISIBLE else View.GONE
        }

        layoutToSortType.forEach { pair ->
            val layout = pair.first
            val button = layout.getChildAt(1) as? ImageView

            sortType = pair.second

            layout.setOnClickListener {
                button?.visibility = View.VISIBLE

                if (button?.tag == FavoriteViewAdapter.SortDirection.ASC.type) {
                    button.tag = FavoriteViewAdapter.SortDirection.DES.type
                    button.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
                    favoriteViewAdapter?.sortList(FavoriteViewAdapter.SortDirection.DES)
                } else if (button?.tag == FavoriteViewAdapter.SortDirection.DES.type) {
                    button.tag = FavoriteViewAdapter.SortDirection.ASC.type
                    button.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
                    favoriteViewAdapter?.sortList(FavoriteViewAdapter.SortDirection.ASC)
                }

                favoriteViewAdapter?.sortList(
                    if (button?.tag == FavoriteViewAdapter.SortDirection.ASC.type)
                        FavoriteViewAdapter.SortDirection.ASC
                    else
                        FavoriteViewAdapter.SortDirection.DES
                )

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
            //isActivated = true
            queryHint = resources.getString(R.string.filter_query_hint)
            //setIconifiedByDefault(false)
            //onActionViewExpanded()
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String): Boolean {
                    favoriteViewAdapter?.filter?.filter(query)
                    favoriteViewAdapter?.query = query
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    favoriteViewAdapter?.filter?.filter(query)
                    favoriteViewAdapter?.query = query
                    return false
                }
            })
        }
    }
}
