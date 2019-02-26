package fho.kdvs.broadcast

import androidx.lifecycle.LiveData
import fho.kdvs.global.database.BroadcastDao
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BroadcastRepository @Inject constructor(
    private val broadcastDao: BroadcastDao,
    private val scraperManager: WebScraperManager
) {
    fun fetchBroadcastsForShow(showId: String): Job? = scraperManager.scrape(URLs.showDetails(showId))

    fun broadcastById(broadcastId: Int): LiveData<BroadcastEntity> =
        broadcastDao.broadcastById(broadcastId)

    fun broadcastsForShow(showId: Int): LiveData<List<BroadcastEntity>> =
        broadcastDao.allBroadcastsForShow(showId)
            .debounce(500L, TimeUnit.MILLISECONDS)
            .toLiveData()

    fun getLatestBroadcastForShow(showId: Int): BroadcastEntity? =
        broadcastDao.getLatestBroadcastForShow(showId)
}