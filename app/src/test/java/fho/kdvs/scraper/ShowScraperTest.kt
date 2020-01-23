package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShowScraperTest : ScraperTest() {
    private lateinit var scrapedShow: ShowEntity
    private val scrapedBroadcasts = mutableListOf<BroadcastEntity>()

    private lateinit var expectedShows: List<ShowEntity>
    private lateinit var expectedBroadcasts: List<BroadcastEntity>

    @Before
    override fun setup() {
        super.setup()

        every {
            showDao.updateShowDetails(
                any(),
                any(),
                any(),
                any()
            )
        } answers {
            val show = ShowEntity(
                id = firstArg() as Int,
                host = secondArg() as String?,
                genre = thirdArg() as String?,
                defaultDesc = arg(3) as String?
            )

            scrapedShow = show
        }

        every { broadcastDao.updateOrInsert(any()) } answers {
            val broadcast = firstArg() as BroadcastEntity

            if (broadcast in scrapedBroadcasts)
                broadcastDao.updateBroadcastDetails(broadcast.broadcastId, broadcast.description, broadcast.imageHref)
            else
                broadcastDao.insert(firstArg())
        }

        every { broadcastDao.updateBroadcastDetails(any(), any(), any()) } answers {
            val match = scrapedBroadcasts.firstOrNull { b ->
                b.broadcastId == firstArg()
            }

            match?.let {
                it.description = secondArg()
                it.imageHref = thirdArg()
            }
        }

        every { broadcastDao.insert(any()) } answers {
            val broadcast = firstArg() as BroadcastEntity
            scrapedBroadcasts.add(broadcast)
        }

        every { kdvsPreferences.setLastShowScrape(any(), any()) } just Runs

        every { kdvsPreferences.getLastShowScrape(any()) } answers { 0 }
    }

    @Test
    fun scrapeShowDetails_fromFile() {
        expectedShows = MockObjects.showDetails

        expectedShows.forEach { show ->
            val html = TestUtils.loadFromResource("${show.id}-show-details.html")
            scraperManager.scrapeShow(html)

            assertEquals("Expected to scrape show details", show, scrapedShow)
        }
    }

    @Test
    fun scrapeBroadcasts_fromFile() {
        expectedBroadcasts = MockObjects.broadcasts

        val showIds = listOf(5280, 5240)
        showIds.forEach { showId ->
            val html = TestUtils.loadFromResource("$showId-show-details.html")

            scraperManager.scrapeShow(html)

            val expectedBroadcastsForShow = expectedBroadcasts.filter { it.showId == showId }
            val scrapedBroadcastsForShow = scrapedBroadcasts.filter { it.showId == showId }

            assertEquals(
                "Expected to find broadcasts",
                expectedBroadcastsForShow,
                scrapedBroadcastsForShow
            )
        }
    }
}