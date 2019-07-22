package fho.kdvs.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentHomeBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.FundraiserEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.StaffEntity
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.news.NewsArticlesAdapter
import fho.kdvs.staff.StaffAdapter
import fho.kdvs.topmusic.TopMusicAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DecimalFormat
import javax.inject.Inject


class HomeFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: HomeViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var fragmentHomeBinding: FragmentHomeBinding

    private var currentShowsAdapter: CurrentShowsAdapter? = null
    private var newsArticlesAdapter: NewsArticlesAdapter? = null
    private var topAddsAdapter: TopMusicAdapter? = null
    private var topAlbumsAdapter: TopMusicAdapter? = null
    private var staffAdapter: StaffAdapter? = null

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

        val snapHelper = PagerSnapHelper()

        val fragment = this

        LoadScreen.displayLoadScreen(root)

        currentShowsAdapter = CurrentShowsAdapter(viewModel) {
            Timber.d("Clicked ${it.item}")
            viewModel.onClickCurrentShow(findNavController(), it.item.id)
        }

        currentShowsRecycler.apply {
            val adapter = currentShowsAdapter as RecyclerView.Adapter<BindingViewHolder<ShowEntity>>

            this.initialize(adapter)
            this.setDefaultPos(1)
            this.setButton(playButton)
            this.setViewsToChangeColor(listOf(
                R.id.currentShowImage,
                R.id.currentShowName,
                R.id.currentShowTime,
                R.id.currentShowHeader
            ))

            onFlingListener = null
            clearOnScrollListeners()
            snapHelper.attachToRecyclerView(this)
            setHasFixedSize(true)
        }

        newsArticlesAdapter = NewsArticlesAdapter(sharedViewModel) {
            Timber.d("Clicked ${it.item}")
        }

        newsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = newsArticlesAdapter

            val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            val drawable = ContextCompat.getDrawable(context, R.drawable.timeslot_divider)

            drawable?.let {
                itemDecorator.setDrawable(it)
                addItemDecoration(itemDecorator)
            }
        }

        topAddsAdapter = TopMusicAdapter {
            Timber.d("Clicked ${it.item}")
            viewModel.onClickTopMusic(findNavController(), it.item)
        }

        topAddsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = topAddsAdapter
        }

        topAlbumsAdapter = TopMusicAdapter {
            Timber.d("Clicked ${it.item}")
            viewModel.onClickTopMusic(findNavController(), it.item)
        }

        topAlbumsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = topAlbumsAdapter
        }

        staffAdapter = StaffAdapter {
            Timber.d("Clicked ${it.item}")
            fragment.showStaffDetails(it.item)
        }

        staffsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = staffAdapter
        }
    }

    @kotlinx.serialization.UnstableDefault
    private fun subscribeToViewModel() {
        viewModel.run {
            combinedLiveData.observe(viewLifecycleOwner, Observer {
                Timber.d("All home observations complete")
                LoadScreen.hideLoadScreen(root)
            })

            currentShows.observe(viewLifecycleOwner, Observer { shows ->
                Timber.d("Got current shows: $shows")
                currentShowsAdapter?.onCurrentShowsChanged(shows)
            })

            newsArticles.observe(viewLifecycleOwner, Observer { articles ->
                Timber.d("Got articles: $articles")
                newsArticlesAdapter?.onNewsChanged(articles)
            })

            topMusicAdds.observe(viewLifecycleOwner, Observer { adds ->
                Timber.d("Got adds: $adds")

                when (adds.isEmpty()) {
                    true -> topAdds.visibility = View.GONE
                    false -> {
                        launch {
                            adds.forEach {
                                sharedViewModel.fetchThirdPartyDataForTopMusic(it, viewModel.topMusicRepository)
                            }
                        }
                        topAddsAdapter?.onTopAddsChanged(adds)
                        topAdds.visibility = View.VISIBLE
                    }
                }
            })

            topMusicAlbums.observe(viewLifecycleOwner, Observer { albums ->
                Timber.d("Got albums: $albums")

                when (albums.isEmpty()) {
                    true -> topAlbums.visibility = View.GONE
                    false -> {
                        launch {
                            albums.forEach {
                                sharedViewModel.fetchThirdPartyDataForTopMusic(it, viewModel.topMusicRepository)
                            }
                        }
                        topAlbumsAdapter?.onTopAlbumsChanged(albums)
                        topAlbums.visibility = View.VISIBLE
                    }
                }
            })

            staff.observe(viewLifecycleOwner, Observer { staff ->
                Timber.d("Got staff: $staff")
                staffAdapter?.onStaffChanged(staff)
            })

            fundraiser.observe(viewLifecycleOwner, Observer { fundraiser ->
                Timber.d("Got fundraiser: $fundraiser")
                val now = TimeHelper.getLocalNow()

                // display fundraiser section only within an n-month window
                // TODO: make preference?
                if (fundraiser != null) {
                    if (fundraiser.dateStart ?: now > now.plusMonths(3) ||
                        fundraiser.dateEnd ?: now < now.minusMonths(3)
                    ) {
                        fundraiserSection.visibility = View.GONE
                    } else {
                        setFundraiserView(fundraiser)
                        fundraiserSection.visibility = View.VISIBLE
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

        val progress = ((fundraiser.current?.toFloat() ?: 0f) / (fundraiser.goal?.toFloat() ?: 1f)) * 100
        fundraiserProgress.progress = if (progress > 100) 100 else progress.toInt()
    }

    private fun showStaffDetails(member: StaffEntity) {
        val args = Bundle()
        args.putParcelable("member", member)

        val newFragment = StaffDetailsFragment()

        newFragment.arguments = args
        newFragment.show(fragmentManager, "staff_details_fragment")
    }
}

