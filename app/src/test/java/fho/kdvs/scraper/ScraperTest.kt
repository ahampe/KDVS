package fho.kdvs.scraper

import android.content.SharedPreferences
import fho.kdvs.extensions.initThreeTen
import fho.kdvs.global.database.*
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.web.WebScraperManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Before

open class ScraperTest {
    internal lateinit var scraperManager: WebScraperManager

    private lateinit var db: KdvsDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var preferencesEditor: SharedPreferences.Editor
    internal lateinit var kdvsPreferences: KdvsPreferences
    internal lateinit var timeslotDao: TimeslotDao
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

        db = mockk()
        kdvsPreferences = mockk()
        sharedPreferences = mockk()
        preferencesEditor = mockk()
        showDao = mockk()
        timeslotDao = mockk()
        broadcastDao = mockk()
        trackDao = mockk()
        newsDao = mockk()
        staffDao = mockk()
        topMusicDao = mockk()
        fundraiserDao = mockk()

        every { db.showDao() } returns showDao
        every { db.timeslotDao() } returns timeslotDao
        every { db.broadcastDao() } returns broadcastDao
        every { db.trackDao() } returns trackDao
        every { db.newsDao() } returns newsDao
        every { db.staffDao() } returns staffDao
        every { db.topMusicDao() } returns topMusicDao
        every { db.fundraiserDao() } returns fundraiserDao
        every { kdvsPreferences.preferences } returns sharedPreferences
        every { sharedPreferences.edit() } returns preferencesEditor

        scraperManager = WebScraperManager(db, kdvsPreferences)
    }
}