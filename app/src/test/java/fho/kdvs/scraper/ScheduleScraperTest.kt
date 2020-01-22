package fho.kdvs.scraper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.ShowEntity
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

        whenever(showDao.insert(any())).doAnswer {
            val show = it.arguments[0] as ShowEntity
            scrapedShows.add(show)
            null
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