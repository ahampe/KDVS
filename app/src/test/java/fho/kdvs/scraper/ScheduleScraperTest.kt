package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.ShowEntity
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScheduleScraperTest : ScraperTest() {
    private val scrapedShows = mutableListOf<ShowEntity>()

    private lateinit var expectedShows: List<ShowEntity>

    @Before
    override fun setup() {
        super.setup()

        every { showDao.updateOrInsert(any()) } answers {
            val show = firstArg<ShowEntity>()

            if (show in scrapedShows)
                showDao.updateShowDefaultImageHref(show.id, show.defaultImageHref)
            else
                showDao.insert(show)
        }

        every { showDao.updateShowDefaultImageHref(any(), any()) } just Runs

        every { showDao.insert(any()) } answers {
            val show = firstArg() as ShowEntity
            scrapedShows.add(show)
        }

        every { timeslotDao.insert(any()) } just Runs

        every {
            kdvsPreferences getProperty "lastScheduleScrape"
        } nullablePropertyType Long::class answers {
            fieldValue
        }

        every {
            kdvsPreferences setProperty "lastScheduleScrape" value any<Long>()
        } answers {
            value
        }
    }

    @Test
    fun scrapeSchedule_fromFile() {
        expectedShows = MockObjects.scheduleShowsWithTimeslots
            .distinctBy { s -> s.first.id }
            .map { s -> s.first }

        val html = TestUtils.loadFromResource("schedule-grid.html")

        scraperManager.scrapeSchedule(html)

        val scrapedNames = scrapedShows
            .map { it.name }

        expectedShows.forEach { show ->
            assertTrue(
                "Expected to find show with name ${show.name}",
                scrapedNames.contains(show.name)
            )
            assertTrue("Expected to find show with details $show", scrapedShows.contains(show))
        }

        assertEquals(
            "Expected to find ${expectedShows.size} shows",
            expectedShows.size,
            scrapedShows.size
        )
    }
}