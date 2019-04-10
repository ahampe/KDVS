package fho.kdvs.home

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentHomeBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.FundraiserEntity
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.news.ContactsAdapter
import fho.kdvs.news.NewsArticlesAdapter
import fho.kdvs.news.TopMusicAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import java.text.DecimalFormat
import javax.inject.Inject

class HomeFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: HomeViewModel

    private var newsArticlesAdapter: NewsArticlesAdapter? = null
    private var topAddsAdapter: TopMusicAdapter? = null
    private var topAlbumsAdapter: TopMusicAdapter? = null
    private var contactsAdapter: ContactsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(HomeViewModel::class.java)
            .also {it.fetchHomeData()}

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        val sharedViewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)

        binding.apply {
            sharedVm = sharedViewModel
        }

        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsArticlesAdapter = NewsArticlesAdapter {
            Timber.d("Clicked ${it.item}")
        }

        newsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = newsArticlesAdapter
        }
        
        topAddsAdapter = TopMusicAdapter { 
            Timber.d("Clicked ${it.item}")
        }
        
        topAddsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = topAddsAdapter
        }

        topAlbumsAdapter = TopMusicAdapter {
            Timber.d("Clicked ${it.item}")
        }

        topAlbumsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = topAlbumsAdapter
        }
        
        contactsAdapter = ContactsAdapter {
            Timber.d("Clicked ${it.item}")
        }

        contactsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = contactsAdapter
        }
    }

    private fun subscribeToViewModel() {
        viewModel.newsArticles.observe(this, Observer { articles ->
            Timber.d("Got articles: $articles")
            newsArticlesAdapter?.onNewsChanged(articles)
        })

        viewModel.topMusicAdds.observe(this, Observer { adds ->
            Timber.d("Got adds: $adds")
            topAddsAdapter?.onTopAddsChanged(adds)
        })

        viewModel.topMusicAlbums.observe(this, Observer { albums ->
            Timber.d("Got albums: $albums")
            topAlbumsAdapter?.onTopAlbumsChanged(albums)
        })

        viewModel.contacts.observe(this, Observer { staff ->
            Timber.d("Got staff: $staff")
            contactsAdapter?.onContactsChanged(staff)
        })

        viewModel.fundraiser.observe(this, Observer { fundraiser ->
            Timber.d("Got fundraiser: $fundraiser")
            val now = LocalDate.now()

            // display fundraiser section within a two-month window
            if (fundraiser.dateStart ?: now <= now.plusMonths(1) ||
                fundraiser.dateEnd ?: now >= now.minusMonths(1)){
                fundraiserSection.visibility = View.VISIBLE
                setFundraiserText(fundraiser)
            }
        })
    }

    private fun setFundraiserText(fundraiser: FundraiserEntity){
        val startMonthStr = TimeHelper.monthIntToStr(fundraiser.dateStart?.monthValue)
            .toLowerCase()
            .capitalize()
        val endMonthStr = TimeHelper.monthIntToStr(fundraiser.dateEnd?.monthValue)
            .toLowerCase()
            .capitalize()
        val dayStart = fundraiser.dateStart?.dayOfMonth.toString()
        val dayEnd = fundraiser.dateEnd?.dayOfMonth.toString()
        val year = fundraiser.dateStart?.year

        if (startMonthStr == endMonthStr)
            fundraiserDates.text = fundraiserDates.context.resources.getString(
                R.string.fundraiser_dates_same_month,
                startMonthStr,
                dayStart,
                dayEnd,
                year
            )
        else
            fundraiserDates.text = fundraiserDates.context.resources.getString(
                R.string.fundraiser_dates_diff_month,
                startMonthStr,
                dayStart,
                endMonthStr,
                dayEnd,
                year
            )

        val goalStr = DecimalFormat(",###")
            .format(fundraiser.goal?.toDouble())
        var currentStr = DecimalFormat(",###")
            .format(fundraiser.current?.toDouble())

        fundraiserTotals.text = fundraiserTotals.context.resources.getString(
            R.string.fundraiser_totals,
            goalStr,
            currentStr
        )
    }
}

