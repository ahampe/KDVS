package fho.kdvs.scraper

import fho.kdvs.model.database.KdvsDatabase
import fho.kdvs.model.database.daos.BroadcastDao
import fho.kdvs.model.database.daos.ShowDao
import fho.kdvs.model.web.WebScraperManager
import org.junit.Before
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

open class ScraperTest {
    internal lateinit var scraperManager: WebScraperManager

    internal lateinit var db: KdvsDatabase
    internal lateinit var showDao: ShowDao
    internal lateinit var broadcastDao: BroadcastDao

    @Before
    open fun setup() {
        db = mock(KdvsDatabase::class.java)
        showDao = mock(ShowDao::class.java)
        broadcastDao = mock(BroadcastDao::class.java)

        `when`(db.showDao()).thenReturn(showDao)
        `when`(db.broadcastDao()).thenReturn(broadcastDao)

        scraperManager = WebScraperManager(db)
    }
}