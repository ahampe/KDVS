package fho.kdvs.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentHomeBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.FundraiserEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.news.NewsArticlesAdapter
import fho.kdvs.news.StaffAdapter
import fho.kdvs.news.TopMusicAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import timber.log.Timber
import java.text.DecimalFormat
import javax.inject.Inject

class HomeFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: HomeViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var fragmentHomeBinding: FragmentHomeBinding

    private var currentShowAdapter: CurrentShowAdapter? = null
    private var newsArticlesAdapter: NewsArticlesAdapter? = null
    private var topAddsAdapter: TopMusicAdapter? = null
    private var topAlbumsAdapter: TopMusicAdapter? = null
    private var staffsAdapter: StaffAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(HomeViewModel::class.java)
            .also {it.fetchHomeData()}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedViewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)

        fragmentHomeBinding.apply {
            vm = viewModel
            sharedVm = sharedViewModel
            urlObj = URLs
        }

        fragmentHomeBinding.lifecycleOwner = this

        subscribeToViewModel()

        return fragmentHomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentShowAdapter = CurrentShowAdapter(viewModel) {
            Timber.d("Clicked ${it.item}")
            viewModel.onClickCurrentShow(findNavController(), it.item.id)
        }

        currentShowRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = currentShowAdapter
        }

        newsArticlesAdapter = NewsArticlesAdapter(sharedViewModel) {
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

        staffsAdapter = StaffAdapter(sharedViewModel) {
            Timber.d("Clicked ${it.item}")
        }

        staffsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = staffsAdapter
        }
    }

    private fun subscribeToViewModel() {
        viewModel.run {
            currentShow.observe(viewLifecycleOwner, Observer { show ->
                Timber.d("Got currently playing show: $show")
                currentShowAdapter?.onCurrentShowChanged(show)
            })

            newsArticles.observe(viewLifecycleOwner, Observer { articles ->
                Timber.d("Got articles: $articles")
                newsArticlesAdapter?.onNewsChanged(articles)
            })

            topMusicAdds.observe(viewLifecycleOwner, Observer { adds ->
                Timber.d("Got adds: $adds")
                launch{ fetchThirdPartyData(adds) }
                topAddsAdapter?.onTopAddsChanged(adds)
            })

            topMusicAlbums.observe(viewLifecycleOwner, Observer { albums ->
                Timber.d("Got albums: $albums")
                topAlbumsAdapter?.onTopAlbumsChanged(albums)
            })

            staff.observe(viewLifecycleOwner, Observer { staff ->
                Timber.d("Got staff: $staff")
                staffsAdapter?.onStaffChanged(staff)
            })

            fundraiser.observe(viewLifecycleOwner, Observer { fundraiser ->
                Timber.d("Got fundraiser: $fundraiser")
                val now = LocalDate.now()

                // display fundraiser section only within a two-month window
                // TODO: make preference?
                if (fundraiser != null) {
                    if (fundraiser.dateStart ?: now > now.plusMonths(1) ||
                        fundraiser.dateEnd ?: now < now.minusMonths(1)
                    ) {
                        fundraiserSection.visibility = View.GONE
                    } else {
                        setFundraiserView(fundraiser)
                    }
                }
            })
        }
    }

    private fun setFundraiserView(fundraiser: FundraiserEntity){
        val startMonthStr = TimeHelper.monthIntToStr(fundraiser.dateStart?.monthValue)
            .toLowerCase()
            .capitalize()
        val endMonthStr = TimeHelper.monthIntToStr(fundraiser.dateEnd?.monthValue)
            .toLowerCase()
            .capitalize()
        val dayStart = fundraiser.dateStart
            ?.dayOfMonth
            .toString()
        val dayEnd = fundraiser.dateEnd
            ?.dayOfMonth
            .toString()
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
        val currentStr = DecimalFormat(",###")
            .format(fundraiser.current?.toDouble())

        fundraiserCurrent.text = fundraiserCurrent.context.resources.getString(
            R.string.fundraiser_total,
            currentStr
        )

        fundraiserGoal.text = fundraiserCurrent.context.resources.getString(
            R.string.fundraiser_total,
            goalStr
        )

        val progress = ((fundraiser.current?.toFloat() ?: 0.toFloat()) / (fundraiser.goal?.toFloat() ?: 1.toFloat())) * 100
        fundraiserProgress.progress = if (progress > 100) 100 else progress.toInt()
    }
}

