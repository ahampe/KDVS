package fho.kdvs.show

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.NewsDao
import fho.kdvs.global.database.NewsEntity
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsDao: NewsDao,
    private val scraperManager: WebScraperManager,
    private val kdvsPreferences: KdvsPreferences
) : BaseRepository() {

    /** Runs a news scrape if it hasn't been fetched recently. */
    fun scrapeNews() = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.lastNewsScrape ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeNews()?.join()
        } else {
            Timber.d("News has already been scraped recently; skipping")
        }
    }

    /**
     * Runs a news scrape without checking when it was last performed.
     * The only acceptable public usage of this method is when user explicitly refreshes.
     */
    fun forceScrapeNews(): Job? = scraperManager.scrape(URLs.NEWS)

    fun getAllNews(): LiveData<List<NewsEntity>> =
        newsDao.getAll()

    fun getAllNewsPastDate(date: LocalDate): LiveData<List<NewsEntity>> =
        newsDao.getAllNewsPastDate(date)
}