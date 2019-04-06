package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.global.database.ContactEntity
import fho.kdvs.global.database.NewsEntity
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.show.ContactRepository
import fho.kdvs.show.NewsRepository
import fho.kdvs.show.TopMusicRepository
import io.reactivex.Flowable
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

    lateinit var newsArticles: Flowable<List<NewsEntity>>
    lateinit var topMusicAdds: Flowable<List<TopMusicEntity>>
    lateinit var topMusicAlbums: Flowable<List<TopMusicEntity>>
    lateinit var contacts: Flowable<List<ContactEntity>>

    fun fetchHomeData() {
        fetchNewsArticles()
        newsArticles = newsRepository.getAllNewsPastDate(
            OffsetDateTime.now().minusMonths(3).toLocalDate())
        // TODO: Make this a preference?

        fetchTopMusicItems()
        topMusicAdds = topMusicRepository.getMostRecentTopAdds()
        topMusicAlbums = topMusicRepository.getMostRecentTopAlbums()

        fetchContacts()
        contacts = contactRepository.getContacts()
    }

    /** Signals the [News Repository] to scrape the news page(s). */
    private fun fetchNewsArticles() = newsRepository.scrapeNews()

    /** Signals the [TopMusic Repository] to scrape the top music pages. */
    private fun fetchTopMusicItems() = topMusicRepository.scrapeTopMusic()

    /** Signals the [Contact Repository] to scrape the top music pages. */
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