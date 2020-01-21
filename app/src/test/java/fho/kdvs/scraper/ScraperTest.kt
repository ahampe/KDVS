package fho.kdvs.scraper

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import fho.kdvs.extensions.initThreeTen
import fho.kdvs.global.database.*
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.web.WebScraperManager
import org.junit.Before

open class ScraperTest {
    internal lateinit var scraperManager: WebScraperManager

    private lateinit var db: KdvsDatabase
    private lateinit var preferences: KdvsPreferences
    private lateinit var timeslotDao: TimeslotDao
    internal lateinit var showDao: ShowDao
    internal lateinit var broadcastDao: BroadcastDao
    internal lateinit var trackDao: TrackDao
    internal lateinit var newsDao: NewsDao
    internal lateinit var staffDao: StaffDao
    internal lateinit var topMusicDao: TopMusicDao
    internal lateinit var fundraiserDao: FundraiserDao

    @Before
    open fun setup() {
        initThreeTen()

        db = mock()
        preferences = mock()
        showDao = mock()
        timeslotDao = mock()
        broadcastDao = mock()
        trackDao = mock()
        newsDao = mock()
        staffDao = mock()
        topMusicDao = mock()
        fundraiserDao = mock()

        whenever(db.showDao()).doReturn(showDao)
        whenever(db.timeslotDao()).doReturn(timeslotDao)
        whenever(db.broadcastDao()).doReturn(broadcastDao)
        whenever(db.trackDao()).doReturn(trackDao)
        whenever(db.newsDao()).doReturn(newsDao)
        whenever(db.staffDao()).doReturn(staffDao)
        whenever(db.topMusicDao()).doReturn(topMusicDao)
        whenever(db.fundraiserDao()).doReturn(fundraiserDao)

        scraperManager = WebScraperManager(db, preferences)
    }
}