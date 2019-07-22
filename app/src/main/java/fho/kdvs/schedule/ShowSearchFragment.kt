package fho.kdvs.schedule

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
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.ShowEntity
import kotlinx.android.synthetic.main.fragment_show_search.*
import timber.log.Timber
import javax.inject.Inject

class ShowSearchFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var vm: ShowSearchViewModel
    private lateinit var sharedVm: SharedViewModel

    private var showSearchViewAdapter: ShowSearchViewAdapter? = null
    var hashedShows = mutableMapOf<String, ArrayList<ShowEntity>>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProviders.of(this, vmFactory)
            .get(ShowSearchViewModel::class.java)

        sharedVm = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)
            .also { it.fetchShows() } // TODO: do this in background? just needs to be finished before user searches

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeSearchBar()
    }

    private fun subscribeToViewModel(){
        val fragment = this

        vm.run {
            sharedVm.getCurrentQuarterYear().observe(fragment, Observer { currentQuarterYear ->
                vm.getShowsForCurrentQuarterYear(currentQuarterYear).observe(fragment, Observer { shows ->
                    // Pair each show with an int corresponding to number of shows in its timeslot
                    val showsWithTimeSlotSize = shows.groupBy { s -> s.timeStart }
                        .map { m ->
                            val list = mutableListOf<Pair<ShowEntity, Int>>()
                            m.value.forEach {
                                list.add(Pair(it, m.value.size))
                            }
                            list
                        }.flatten()

                    showSearchViewAdapter = ShowSearchViewAdapter(showsWithTimeSlotSize, fragment) {
                        Timber.d("clicked ${it.item}")
                        vm.onClickShow(findNavController(), it.item)
                    }

                    resultsRecycler.run {
                        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        adapter = showSearchViewAdapter
                    }
                })
            })

        }
    }

    private fun initializeSearchBar(){
        searchBar?.run {
            isActivated = true
            queryHint = resources.getString(R.string.search_query_hint)
            setIconifiedByDefault(false)
            onActionViewExpanded()
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String): Boolean {
                    // filter recycler view when query submitted
                    showSearchViewAdapter?.filter?.filter(query)
                    showSearchViewAdapter?.query = query
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    // filter recycler view when text is changed
                    showSearchViewAdapter?.filter?.filter(query)
                    showSearchViewAdapter?.query = query
                    return false
                }
            })
        }
    }
}