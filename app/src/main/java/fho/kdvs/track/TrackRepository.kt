package fho.kdvs.track

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.TrackDao
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val webScraperManager: WebScraperManager,
    private val kdvsPreferences: KdvsPreferences
) : BaseRepository() {

    /** Runs a broadcast scrape if it hasn't been fetched recently. */
    fun scrapePlaylist(broadcastId: String) = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.getLastBroadcastScrape(broadcastId) ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapePlaylist(broadcastId)?.join()
        } else {
            Timber.d("Playlist $broadcastId has already been scraped recently; skipping")
        }
    }

    /**
     * Runs a broadcast scrape without checking when it was last performed.
     * The only acceptable public usage of this method is when user explicitly refreshes.
     */
    fun forceScrapePlaylist(broadcastId: String): Job? = webScraperManager.scrape(URLs.broadcastDetails(broadcastId))

    fun trackById(trackId: Int): LiveData<TrackEntity> = trackDao.trackById(trackId)

    fun spotifyUriById(trackId: Int): LiveData<String> = trackDao.spotifyUriById(trackId)

    fun tracksForBroadcast(broadcastId: Int): LiveData<List<TrackEntity>> =
        trackDao.allTracksForBroadcast(broadcastId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    fun updateTrackAlbum(trackId: Int?, album: String?) = trackDao.updateAlbum(trackId, album)

    fun updateTrackImageHref(trackId: Int?, href: String?) = trackDao.updateImageHref(trackId, href)

    fun updateTrackLabel(trackId: Int?, label: String?) = trackDao.updateLabel(trackId, label)

    fun updateTrackSpotifyUri(trackId: Int?, spotifyUri: String?) = trackDao.updateSpotifyUri(trackId, spotifyUri)

    fun updateTrackYear(trackId: Int?, year: Int?) = trackDao.updateYear(trackId, year)

    fun onScrapeMetadata(trackId: Int?) = trackDao.onScrapeMetadata(trackId)
}