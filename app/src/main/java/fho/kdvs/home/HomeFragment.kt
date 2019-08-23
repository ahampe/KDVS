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
import fho.kdvs.global.extensions.fade
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.news.NewsArticlesAdapter
import fho.kdvs.staff.StaffAdapter
import fho.kdvs.topmusic.TopMusicAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import net.cachapa.expandablelayout.ExpandableLayout
import timber.log.Timber
import java.text.DecimalFormat
import javax.inject.Inject

@kotlinx.serialization.UnstableDefault
class HomeFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

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
            .also {
                it.fetchHomeData()
            }
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

        LoadScreen.displayLoadScreen(homeRoot)

        settingsIcon.setOnClickListener { viewModel.onClickSettings(findNavController()) }

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

        playButton.setOnClickListener {
            sharedViewModel.playLiveShowFromHome(activity)
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

        newsHeader.setOnClickListener {
            if (newsExpandable.isExpanded)
                newsExpandable.collapse()
            else
                newsExpandable.expand()
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

        staffAdapter = StaffAdapter(sharedViewModel) {
            Timber.d("Clicked ${it.item}")

            val parent = it.view.parent as? View
            val expandable = parent?.findViewById(R.id.memberExpandable) as? ExpandableLayout

            expandable?.let { exp ->
                if (exp.isExpanded) {
                    exp.collapse()
                } else {
                    exp.expand()
                }
            }
        }

        staffRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = staffAdapter
        }

        setExpandableSections()
    }

    private fun subscribeToViewModel() {
        viewModel.run {
            combinedLiveData.observe(viewLifecycleOwner, Observer {
                Timber.d("All home observations complete")
                LoadScreen.hideLoadScreen(homeRoot)
            })

            currentShows.observe(viewLifecycleOwner, Observer { shows ->
                Timber.d("Got current shows: $shows")
                currentShowsAdapter?.onCurrentShowsChanged(shows)
            })

            newsArticles.observe(viewLifecycleOwner, Observer { articles ->
                Timber.d("Got articles: $articles")

                val highestId = articles.maxBy { a -> a.newsId}?.newsId

                // Display info icon when there are unviewed articles
                if (highestId != kdvsPreferences.lastObservedNewsId) {
                    newsNotification.fade(true)
                }

                newsNotification.tag = highestId

                newsArticlesAdapter?.onNewsChanged(articles)
            })

            topMusicAdds.observe(viewLifecycleOwner, Observer { adds ->
                Timber.d("Got adds: $adds")

                val highestId = adds.maxBy { a -> a.topMusicId}?.topMusicId

                if (highestId != kdvsPreferences.lastObservedTopAddsId) {
                    topAddsNotification.fade(true)
                }

                topAddsNotification.tag = highestId

                when (adds.isEmpty()) {
                    true -> topAdds.visibility = View.GONE
                    false -> {
                        launch {
                            adds.forEach {
                                sharedViewModel.fetchThirdPartyDataForTopMusic(it)
                            }
                        }

                        topAddsAdapter?.onTopAddsChanged(adds)
                        topAdds.visibility = View.VISIBLE
                    }
                }
            })

            topMusicAlbums.observe(viewLifecycleOwner, Observer { albums ->
                Timber.d("Got albums: $albums")

                val highestId = albums.maxBy { a -> a.topMusicId}?.topMusicId

                // Display info icon when there are unviewed fundraiser progress
                if (highestId != kdvsPreferences.lastObservedTopAlbumsId) {
                    topAlbumsNotification.fade(true)
                }

                topAlbumsNotification.tag = highestId

                when (albums.isEmpty()) {
                    true -> topAlbums.visibility = View.GONE
                    false -> {
                        launch {
                            albums.forEach {
                                sharedViewModel.fetchThirdPartyDataForTopMusic(it)
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
                fundraiser?.let {
                    // Display info icon when there is unviewed fundraiser progress
                    if (fundraiser.current != kdvsPreferences.lastObservedFundraiserAmount) {
                        fundraiserNotification.fade(true)
                    }

                    fundraiserNotification.tag = fundraiser.current

                    val window = (kdvsPreferences.fundraiserWindow ?: 2).toLong()

                    if (fundraiser.dateStart ?: now > now.plusMonths(window) ||
                        fundraiser.dateEnd ?: now < now.minusMonths(window)
                    ) {
                        fundraiserSection.visibility = View.GONE
                    } else {
                        setFundraiserView(fundraiser)
                        fundraiserSection.visibility = View.VISIBLE
                    }
                }
            })
        }

        sharedViewModel.run {
            // When new quarter-years happen (which should only happen when a new quarter starts),
            // cancel nonrecurring subscriptions
            allQuarterYearsLiveData.observe(viewLifecycleOwner, Observer {
                it.first().let { q ->
                    if (q != kdvsPreferences.mostRecentQuarterYear) {
                        this.onNewQuarter(context)
                        kdvsPreferences.mostRecentQuarterYear = q
                    }
                }
            })
        }
    }

    private fun setExpandableSections() {
        fundraiserHeader.setOnClickListener {
            onExpandClick(fundraiserExpandable)

            fundraiserNotification.tag?.let {
                if (it is Int) {
                    kdvsPreferences.lastObservedFundraiserAmount = it
                }
            }

            fundraiserNotification.visibility = View.GONE
        }

        newsHeader.setOnClickListener {
            onExpandClick(newsExpandable)

            newsNotification.tag?.let {
                if (it is Int) {
                    kdvsPreferences.lastObservedNewsId = it
                }
            }

            newsNotification.visibility = View.GONE
        }

        topAddsHeader.setOnClickListener {
            onExpandClick(topAddsExpandable)

            topAddsNotification.tag?.let {
                if (it is Int) {
                    kdvsPreferences.lastObservedTopAddsId = it
                }
            }

            topAddsNotification.visibility = View.GONE
        }

        topAlbumsHeader.setOnClickListener {
            onExpandClick(topAlbumsExpandable)

            topAlbumsNotification.tag?.let {
                if (it is Int) {
                    kdvsPreferences.lastObservedTopAlbumsId = it
                }
            }

            topAlbumsNotification.visibility = View.GONE
        }

        staffHeader.setOnClickListener {
            onExpandClick(staffExpandable)
        }

        contactHeader.setOnClickListener {
            onExpandClick(contactExpandable)
        }
    }

    private fun onExpandClick(expandable: ExpandableLayout) {
        if (expandable.isExpanded) {
            expandable.collapse()
        } else {
            expandable.expand()
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

        fundraiserCurrent.tag = fundraiser.current

        fundraiserGoal.text = fundraiserCurrent.context.resources.getString(
            R.string.fundraiser_total,
            goalStr
        )

        val progress = ((fundraiser.current?.toFloat() ?: 0f) / (fundraiser.goal?.toFloat() ?: 1f)) * 100
        fundraiserProgress.progress = if (progress > 100) 100 else progress.toInt()
    }
}

