package fho.kdvs.broadcast

import androidx.lifecycle.LiveData
import fho.kdvs.global.database.BroadcastDao
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import javax.inject.Inject

class BroadcastRepository @Inject constructor(
    private val broadcastDao: BroadcastDao,
    private val scraperManager: WebScraperManager
) {
    fun fetchBroadcastsForShow(showId: String) = scraperManager.scrape(URLs.showDetails(showId))

    fun broadcastById(broadcastId: Int): LiveData<BroadcastEntity> =
        broadcastDao.broadcastById(broadcastId)

    fun broadcastsForShow(showId: Int): LiveData<List<BroadcastEntity>> =
        broadcastDao.broadcastsForShowLiveData(showId)
}