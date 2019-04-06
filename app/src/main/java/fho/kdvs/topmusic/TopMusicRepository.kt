package fho.kdvs.show

import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import io.reactivex.Flowable
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopMusicRepository @Inject constructor(
    private val topMusicDao: TopMusicDao,
    private val scraperManager: WebScraperManager,
    private val kdvsPreferences: KdvsPreferences
) : BaseRepository() {

    // TODO: Make this weekly?
    /** Runs top music scrapes if they haven't been fetched recently. */
    fun scrapeTopMusic(){
        scrapeTopAdds()
        scrapeTopAlbums()
    }

    private fun scrapeTopAdds() = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.lastTopFiveAddsScrape ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeTopAdds()?.join()
        } else {
            Timber.d("Top adds has already been scraped recently; skipping")
        }
    }

    private fun scrapeTopAlbums() = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.lastTopThirtyAlbumsScrape ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeTopAlbums()?.join()
        } else {
            Timber.d("Top albums has already been scraped recently; skipping")
        }
    }

    /**
     * Runs a topMusic scrape without checking when it was last performed.
     * The only acceptable public usage of this method is when user explicitly refreshes.
     */
    fun forceScrapeTopAdds(): Job? = scraperManager.scrape(URLs.TOP_ADDS)

    fun forceScrapeTopAlbums(): Job? = scraperManager.scrape(URLs.TOP_ALBUMS)

    fun getMostRecentTopAdds(): Flowable<List<TopMusicEntity>> =
        topMusicDao.getMostRecentTopAdds()

    fun getMostRecentTopAlbums(): Flowable<List<TopMusicEntity>> =
        topMusicDao.getMostRecentTopAlbums()
}