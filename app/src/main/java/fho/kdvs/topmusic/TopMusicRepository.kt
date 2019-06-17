package fho.kdvs.show

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.TopMusicDao
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.MusicBrainzReleaseData
import fho.kdvs.global.web.SpotifyData
import fho.kdvs.global.web.WebScraperManager
import fho.kdvs.topmusic.TopMusicType
import fho.kdvs.topmusic.limit
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
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

    /** Runs top music scrapes if they haven't been fetched in a week. */
    fun scrapeTopMusic(){
        scrapeTopAdds()
        scrapeTopAlbums()
    }

    private fun scrapeTopAdds() = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.lastTopFiveAddsScrape ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.WEEKLY_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeTopAdds()?.join()
        } else {
            Timber.d("Top adds has already been scraped recently; skipping")
        }
    }

    private fun scrapeTopAlbums() = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.lastTopThirtyAlbumsScrape ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.WEEKLY_SCRAPE_FREQ

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
    private fun forceScrapeTopAdds(): Job? = scraperManager.scrape(URLs.TOP_ADDS)

    private fun forceScrapeTopAlbums(): Job? = scraperManager.scrape(URLs.TOP_ALBUMS)

    fun getMostRecentTopAdds(): LiveData<List<TopMusicEntity>> =
        topMusicDao.getMostRecentTopMusicForType(TopMusicType.ADD, TopMusicType.ADD.limit)

    fun getMostRecentTopAlbums(): LiveData<List<TopMusicEntity>> =
        topMusicDao.getMostRecentTopMusicForType(TopMusicType.ALBUM, TopMusicType.ALBUM.limit)

    fun getTopAddsForWeekOf(weekOf: LocalDate?, type: TopMusicType): LiveData<List<TopMusicEntity>> =
        topMusicDao.getTopMusicForWeekOfType(weekOf, type)

    fun updateTopMusicAlbum(id: Int, title: String?) {
        title?.let{
            topMusicDao.updateTopMusicAlbum(id, title)
        }
    }

    fun updateTopMusicLabel(id: Int, label: String?) {
        label?.let{
            topMusicDao.updateTopMusicLabel(id, label)
        }
    }

    fun updateTopMusicYear(id: Int, year: Int?) {
        year?.let{
            topMusicDao.updateTopMusicYear(id, year)
        }
    }

    fun updateTopMusicImageHref(id: Int, imageHref: String?) {
        imageHref?.let{
            topMusicDao.updateTopMusicImageHref(id, imageHref)
        }
    }

    fun updateTopMusicMusicBrainzData(id: Int, mbData: MusicBrainzReleaseData?) {
        mbData?.let {
            topMusicDao.updateTopMusicMusicBrainzData(id, mbData)
        }
    }

    fun updateTopMusicSpotifyData(id: Int, spotifyData: SpotifyData?) {
        spotifyData?.let{
            topMusicDao.updateTopMusicSpotifyData(id, spotifyData)
        }
    }
}