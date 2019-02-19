package fho.kdvs.track

import androidx.lifecycle.LiveData
import fho.kdvs.global.database.TrackDao
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val webScraperManager: WebScraperManager
) {
    fun fetchTracksForBroadcast(broadcastId: String) = webScraperManager.scrape(URLs.broadcastDetails(broadcastId))

    fun tracksForBroadcast(broadcastId: Int): LiveData<List<TrackEntity>> =
        trackDao.allTracksForBroadcast(broadcastId)
            .debounce(500L, TimeUnit.MILLISECONDS)
            .toLiveData()
}