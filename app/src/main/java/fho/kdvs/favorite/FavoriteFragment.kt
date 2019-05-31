package fho.kdvs.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.database.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.database.getTrack
import kotlinx.android.synthetic.main.fragment_show_search.*
import timber.log.Timber
import javax.inject.Inject

class FavoriteFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: FavoriteViewModel

    private var favoriteViewAdapter: FavoriteViewAdapter? = null
    var hashedTracks = mutableMapOf<String, ArrayList<ShowBroadcastTrackFavoriteJoin>>()

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

        initializeSearchBar()
    }

    private fun subscribeToViewModel(){
        val fragment = this

        viewModel.run {
            getShowBroadcastTrackFavoriteJoins().observe(fragment, Observer { joins ->
                favoriteViewAdapter = FavoriteViewAdapter(joins, fragment) {
                    Timber.d("clicked ${it.item}")
                    viewModel.onClickTrack(findNavController(), it.item.getTrack())
                }

                resultsRecycler.run {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = favoriteViewAdapter
                }
            })
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
