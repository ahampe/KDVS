package fho.kdvs.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.global.database.*
import fho.kdvs.global.web.*
import fho.kdvs.show.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

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
) : AndroidViewModel(application), CoroutineScope {

    // The news page will be updated with news articles at indeterminate (probably infrequent) intervals
    // but other pages will be weekly / quarterly, in monolithic updates
    lateinit var currentShow: LiveData<ShowEntity>
    lateinit var newsArticles: LiveData<List<NewsEntity>>
    lateinit var topMusicAdds: LiveData<List<TopMusicEntity>>
    lateinit var topMusicAlbums: LiveData<List<TopMusicEntity>>
    lateinit var staff: LiveData<List<StaffEntity>>
    lateinit var fundraiser: LiveData<FundraiserEntity>

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun fetchHomeData() {
        fetchNewsArticles()
        fetchTopMusicItems()
        fetchStaff()
        fetchFundraiser()

        currentShow = showRepository.liveShowLiveData
        newsArticles = newsRepository.getAllNewsPastDate(
            OffsetDateTime.now().minusMonths(6).toLocalDate()) // TODO: Make this a preference?
        topMusicAdds = topMusicRepository.getMostRecentTopAdds()
        topMusicAlbums = topMusicRepository.getMostRecentTopAlbums()
        staff = staffRepository.getStaff()
        fundraiser = fundraiserRepository.getFundraiser()
    }

    suspend fun fetchThirdPartyData(topMusicItems: List<TopMusicEntity>) {
        topMusicItems.forEach { item ->
            if (!item.hasScrapedMetadata) {
                var mbData: MusicBrainzData? = null
                var spotifyData: SpotifyData? = null

                launch {
                    val musicBrainzJob = launch {
                        mbData = MusicBrainz.getMusicData(item.album, item.artist, ThirdPartyQueryType.ALBUM)
                    }

                    val spotifyJob = launch {
                        spotifyData = Spotify.getMusicData(item.album, item.artist, ThirdPartyQueryType.ALBUM)
                    }

                    musicBrainzJob.join()
                    spotifyJob.join()
                    
                    if (!mbData?.albumTitle.isNullOrBlank()) {
                        launch { topMusicRepository.updateTopMusicAlbum(item.topMusicId, mbData?.albumTitle)}
                    } else if (!spotifyData?.albumTitle.isNullOrBlank()) {
                        launch { topMusicRepository.updateTopMusicAlbum(item.topMusicId, spotifyData?.albumTitle)}
                    }

                    if (!mbData?.label.isNullOrBlank()) {
                        launch { topMusicRepository.updateTopMusicLabel(item.topMusicId, mbData?.label)}
                    }

                    if (!mbData?.imageHref.isNullOrBlank()) {
                        launch { topMusicRepository.updateTopMusicImageHref(item.topMusicId, mbData?.imageHref)}
                    } else if (!spotifyData?.imageHref.isNullOrBlank()) {
                        launch { topMusicRepository.updateTopMusicImageHref(item.topMusicId, spotifyData?.imageHref)}
                    }

                    if (mbData?.year != null && mbData?.year != -1) {
                        launch { topMusicRepository.updateTopMusicYear(item.topMusicId, mbData?.year)}
                    } else if (spotifyData?.year != null && spotifyData?.year != -1) {
                        launch { topMusicRepository.updateTopMusicYear(item.topMusicId, spotifyData?.year)}
                    }

                    if (!spotifyData?.spotifyUri.isNullOrBlank()) {
                        launch { topMusicRepository.updateTopMusicSpotifyUri(item.topMusicId, spotifyData?.spotifyUri)}
                    }
                }
            }
        }
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
        if (navController.currentDestination?.id == R.id.homeFragment)
            navController.navigate(navAction)
    }
}