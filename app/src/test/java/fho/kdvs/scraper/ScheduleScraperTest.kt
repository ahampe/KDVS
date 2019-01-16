package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.model.database.entities.ShowEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`

class ScheduleScraperTest : ScraperTest() {
    @Test
    fun scrapeSchedule_fromFile() {
        val scrapedShows = mutableListOf<ShowEntity>()

        `when`(showDao.insert(TestUtils.any())).thenAnswer {
            val show: ShowEntity = it.getArgument(0)
            scrapedShows.add(show)
        }

        val html = TestUtils.loadFromResource("schedule-grid.html")

        scraperManager.scrapeSchedule(html)

        val scrapedNames = scrapedShows.map { it.name }

        val expectedShows = MockObjects.scheduleShows

        expectedShows.forEach { show ->
            assertTrue("Expected to find show with name ${show.name}", scrapedNames.contains(show.name))
            assertTrue("Expected to find show with details $show", scrapedShows.contains(show))
        }

        assertEquals("Expected to find ${expectedShows.size} shows", expectedShows.size, scrapedShows.size)
    }
}