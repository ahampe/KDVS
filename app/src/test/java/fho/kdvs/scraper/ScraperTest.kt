package fho.kdvs.scraper

import fho.kdvs.global.database.*
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.web.WebScraperManager
import org.junit.Before
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

open class ScraperTest {
    internal lateinit var scraperManager: WebScraperManager

    private lateinit var db: KdvsDatabase
    private lateinit var preferences: KdvsPreferences
    internal lateinit var showDao: ShowDao
    internal lateinit var broadcastDao: BroadcastDao
    internal lateinit var trackDao: TrackDao
    internal lateinit var newsDao: NewsDao
    internal lateinit var staffDao: StaffDao
    internal lateinit var topMusicDao: TopMusicDao
    internal lateinit var fundraiserDao: FundraiserDao

    @Before
    open fun setup() {
        db = mock(KdvsDatabase::class.java)
        preferences = mock(KdvsPreferences::class.java)
        showDao = mock(ShowDao::class.java)
        broadcastDao = mock(BroadcastDao::class.java)
        trackDao = mock(TrackDao::class.java)
        newsDao = mock(NewsDao::class.java)
        staffDao = mock(StaffDao::class.java)
        topMusicDao = mock(TopMusicDao::class.java)
        fundraiserDao = mock(FundraiserDao::class.java)

        `when`(db.showDao()).thenReturn(showDao)
        `when`(db.broadcastDao()).thenReturn(broadcastDao)
        `when`(db.trackDao()).thenReturn(trackDao)
        `when`(db.newsDao()).thenReturn(newsDao)
        `when`(db.contactDao()).thenReturn(staffDao)
        `when`(db.topMusicDao()).thenReturn(topMusicDao)
        `when`(db.fundraiserDao()).thenReturn(fundraiserDao)

        scraperManager = WebScraperManager(db, preferences)
    }
}