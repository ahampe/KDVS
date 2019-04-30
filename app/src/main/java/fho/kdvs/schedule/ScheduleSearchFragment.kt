package fho.kdvs.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.global.KdvsViewModelFactory
import kotlinx.android.synthetic.main.fragment_schedule_search.*
import timber.log.Timber
import javax.inject.Inject

class ScheduleSearchFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: ScheduleSearchViewModel

    private var scheduleSearchViewAdapter: ScheduleSearchViewAdapter? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ScheduleSearchViewModel::class.java)

        initializeSearch()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleSearchViewAdapter = ScheduleSearchViewAdapter {
            Timber.d("clicked ${it.item}")
            viewModel.onClickShow(findNavController(), it.item)
        }
        
        resultsRecycler.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = scheduleSearchViewAdapter
        }
    }

    private fun initializeSearch(){
        search_bar.isActivated = true
        search_bar.queryHint = resources.getString(R.string.search_query_hint)
    }
}