package fho.kdvs.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.fundraiser.FundraiserRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.extensions.isNullOrEmptyGeneric
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.news.NewsRepository
import fho.kdvs.show.ShowRepository
import fho.kdvs.staff.StaffRepository
import fho.kdvs.topmusic.TopMusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * [AndroidViewModel] for holding home-related data.
 */
@kotlinx.serialization.UnstableDefault
class HomeViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val newsRepository: NewsRepository,
    private val topMusicRepository: TopMusicRepository,
    private val staffRepository: StaffRepository,
    private val fundraiserRepository: FundraiserRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    // The news page will be updated with news articles at indeterminate (probably infrequent) intervals
    // but other pages will be weekly / quarterly, in monolithic updates
    lateinit var currentShows: MediatorLiveData<List<ShowEntity>>
    lateinit var newsArticles: LiveData<List<NewsEntity>>
    lateinit var topMusicAdds: LiveData<List<TopMusicEntity>>
    lateinit var topMusicAlbums: LiveData<List<TopMusicEntity>>
    lateinit var staff: LiveData<List<StaffEntity>>
    lateinit var fundraiser: LiveData<FundraiserEntity>
    lateinit var combinedLiveData: MediatorLiveData<Boolean>

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    /** Emits true if all home data streams emitted value; false if at least one such data stream does. */
    fun fetchHomeData(): LiveData<Boolean> {
        fetchShows()
        fetchNewsArticles()
        fetchTopMusicItems()
        fetchStaff()
        fetchFundraiser()

        currentShows = showRepository.currentShowsLiveData
        newsArticles = newsRepository.getAllNewsPastDate(
            TimeHelper.getNow().minusMonths(6).toLocalDate()) // TODO: Make this a preference?
        topMusicAdds = topMusicRepository.getMostRecentTopAdds()
        topMusicAlbums = topMusicRepository.getMostRecentTopAlbums()
        staff = staffRepository.getStaff()
        fundraiser = fundraiserRepository.getFundraiser()

        val dataStreams = listOf(currentShows, newsArticles, topMusicAdds, topMusicAlbums, staff, fundraiser)
        combinedLiveData = MediatorLiveData<Boolean>()
            .apply {
                dataStreams.forEach { liveData ->
                    addSource(liveData) {
                        if (dataStreams.all { d -> d.value != null})
                            postValue(true)
                        else if (dataStreams.any { d -> !d.value.isNullOrEmptyGeneric()}) // Without extension, empty ArrayLists cause this expression to be true
                            postValue(false)
                    }
                }
            }

        return combinedLiveData
    }

    /** Signals the [ShowRepository] to scrape the schedule grid. */
    private fun fetchShows() = showRepository.scrapeSchedule()

    /** Signals the [News Repository] to scrape the news page(s). */
    private fun fetchNewsArticles() = newsRepository.scrapeNews()

    /** Signals the [TopMusic Repository] to scrape the top music pages. */
    private fun fetchTopMusicItems() = topMusicRepository.scrapeTopMusic()

    /** Signals the [Staff Repository] to scrape the staff page. */
    private fun fetchStaff() = staffRepository.scrapeStaff()

    /** Signals the [Fundraiser Repository] to scrape the fundraiser page. */
    private fun fetchFundraiser() = fundraiserRepository.scrapeFundraiser()

    fun onClickSettings(navController: NavController) {
        val navAction = HomeFragmentDirections
            .actionHomeFragmentToSettingsFragment()
        if (navController.currentDestination?.id == R.id.homeFragment)
            navController.navigate(navAction)
    }

    fun onClickCurrentShow(navController: NavController, showId: Int) {
        val navAction = HomeFragmentDirections
            .actionHomeFragmentToShowDetailsFragment(showId)
        if (navController.currentDestination?.id == R.id.homeFragment)
            navController.navigate(navAction)
    }

    fun onClickTopMusic(navController: NavController, topMusic: TopMusicEntity) {
        val navAction = HomeFragmentDirections
            .actionHomeFragmentToTopMusicDetailsFragment(topMusic)
        if (navController.currentDestination?.id == R.id.homeFragment)
            navController.navigate(navAction)
    }
}