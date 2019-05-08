package fho.kdvs.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import fho.kdvs.global.database.*
import fho.kdvs.show.*
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import javax.inject.Inject

/**
 * [AndroidViewModel] for holding home-related data.
 */
class HomeViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val newsRepository: NewsRepository,
    private val topMusicRepository: TopMusicRepository,
    private val staffRepository: StaffRepository,
    private val fundraiserRepository: FundraiserRepository,
    application: Application
) : AndroidViewModel(application) {

    // The news page will be updated with news articles at indeterminate (probably infrequent) intervals
    // but other pages will be weekly / quarterly, in monolithic updates
    lateinit var currentShow: LiveData<ShowEntity>
    lateinit var newsArticles: LiveData<List<NewsEntity>>
    lateinit var topMusicAdds: LiveData<List<TopMusicEntity>>
    lateinit var topMusicAlbums: LiveData<List<TopMusicEntity>>
    lateinit var staff: LiveData<List<StaffEntity>>
    lateinit var fundraiser: LiveData<FundraiserEntity>

    fun fetchHomeData() {
        fetchNewsArticles()
        fetchTopMusicItems()
        fetchStaff()
        fetchFundraiser()

        currentShow = showRepository.playingShowLiveData
        newsArticles = newsRepository.getAllNewsPastDate(
            OffsetDateTime.now().minusMonths(6).toLocalDate()) // TODO: Make this a preference?
        topMusicAdds = topMusicRepository.getMostRecentTopAdds()
        topMusicAlbums = topMusicRepository.getMostRecentTopAlbums()
        staff = staffRepository.getStaff()
        fundraiser = fundraiserRepository.getFundraiser()
    }

    /** Signals the [News Repository] to scrape the news page(s). */
    private fun fetchNewsArticles() = newsRepository.scrapeNews()

    /** Signals the [TopMusic Repository] to scrape the top music pages. */
    private fun fetchTopMusicItems() = topMusicRepository.scrapeTopMusic()

    /** Signals the [Staff Repository] to scrape the staff page. */
    private fun fetchStaff() = staffRepository.scrapeStaff()

    /** Signals the [Fundraiser Repository] to scrape the fundraiser page. */
    private fun fetchFundraiser() = fundraiserRepository.scrapeFundraiser()

    fun onClickCurrentShow(navController: NavController, showId: Int) {
        val navAction = HomeFragmentDirections
            .actionHomeFragmentToShowDetailsFragment(showId)
        navController.navigate(navAction)
    }
}