package fho.kdvs.modules.track

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.TrackDao
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
        val now = TimeHelper.getNow().toEpochSecond()
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
    private fun forceScrapePlaylist(broadcastId: String): Job? =
        webScraperManager.scrape(URLs.broadcastDetails(broadcastId))

    fun trackById(trackId: Int): LiveData<TrackEntity> = trackDao.trackById(trackId)

    fun tracksForBroadcast(broadcastId: Int): LiveData<List<TrackEntity>> =
        trackDao.allTracksForBroadcast(broadcastId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    fun songsForBroadcast(broadcastId: Int): LiveData<List<TrackEntity>> =
        trackDao.allSongsForBroadcast(broadcastId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    fun updateTrackAlbum(id: Int, title: String?) {
        title?.let {
            trackDao.updateAlbum(id, title)
        }
    }

    fun updateTrackLabel(id: Int, label: String?) {
        label?.let {
            trackDao.updateLabel(id, label)
        }
    }

    fun updateTrackYear(id: Int, year: Int?) {
        year?.let {
            trackDao.updateYear(id, year)
        }
    }

    fun updateTrackImageHref(id: Int, imageHref: String?) {
        imageHref?.let {
            trackDao.updateImageHref(id, imageHref)
        }
    }

    fun updateSpotifyAlbumUri(id: Int, spotifyAlbumUri: String) {
        trackDao.updateSpotifyAlbumUri(id, spotifyAlbumUri)
    }

    fun updateSpotifyTrackUri(id: Int, spotifyTrackUri: String) {
        trackDao.updateSpotifyTrackUri(id, spotifyTrackUri)
    }

    fun updateTrackYouTubeId(id: Int, youTubeId: String) {
        trackDao.updateYouTubeId(id, youTubeId)
    }

    fun updateHasThirdPartyInfo(id: Int, hasThirdPartyInfo: Boolean) {
        trackDao.updateHasThirdPartyInfo(id, hasThirdPartyInfo)
    }
}
