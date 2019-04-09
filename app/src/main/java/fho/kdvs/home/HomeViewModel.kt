package fho.kdvs.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.global.database.StaffEntity
import fho.kdvs.global.database.NewsEntity
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.show.ContactRepository
import fho.kdvs.show.NewsRepository
import fho.kdvs.show.TopMusicRepository
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

/**
 * [AndroidViewModel] for holding home-related data.
 */
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val topMusicRepository: TopMusicRepository,
    private val contactRepository: ContactRepository,
    application: Application
) : AndroidViewModel(application) {

    // The news page will be updated with news articles at indeterminate (probably infrequent) intervals
    // but other pages will be weekly / quarterly, in monolithic updates
    lateinit var newsArticles: LiveData<List<NewsEntity>>
    lateinit var topMusicAdds: LiveData<List<TopMusicEntity>>
    lateinit var topMusicAlbums: LiveData<List<TopMusicEntity>>
    lateinit var contacts: LiveData<List<StaffEntity>>

    fun fetchHomeData() {
        fetchNewsArticles()
        fetchTopMusicItems()
        fetchContacts()

        newsArticles = newsRepository.getAllNewsPastDate(
            OffsetDateTime.now().minusMonths(6).toLocalDate()) // TODO: Make this a preference?
        topMusicAdds = topMusicRepository.getMostRecentTopAdds()
        topMusicAlbums = topMusicRepository.getMostRecentTopAlbums()
        contacts = contactRepository.getContacts()
    }

    /** Signals the [News Repository] to scrape the news page(s). */
    private fun fetchNewsArticles() = newsRepository.scrapeNews()

    /** Signals the [TopMusic Repository] to scrape the top music pages. */
    private fun fetchTopMusicItems() = topMusicRepository.scrapeTopMusic()

    /** Signals the [Contact Repository] to scrape the contact page. */
    private fun fetchContacts() = contactRepository.scrapeContact()

    /**
     * Called when a news article is clicked.
     * */
//    fun onClickNews(navController: NavController, timeSlot: TimeSlot) {
//        val navAction = ScheduleFragmentDirections
//            .actionScheduleFragmentToShowDetailsFragment(timeSlot.ids.first())
//        navController.navigate(navAction)
//    }
}