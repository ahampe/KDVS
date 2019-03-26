package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class ShowScraperTest : ScraperTest() {
    private lateinit var scrapedShow: ShowEntity
    private val scrapedBroadcasts = mutableListOf<BroadcastEntity>()

    private lateinit var expectedShows: List<ShowEntity>
    private lateinit var expectedBroadcasts: List<BroadcastEntity>

    @Before
    override fun setup() {
        super.setup()

        `when`(showDao.updateShowDetails(TestUtils.any(), TestUtils.any(), TestUtils.any(), TestUtils.any())).thenAnswer {
            val show = ShowEntity(
                id = it.getArgument(0),
                host = it.getArgument(1),
                genre = it.getArgument(2),
                defaultDesc = it.getArgument(3)
            )
            Any().also { scrapedShow = show } // thenAnswer hack
        }

        `when`(broadcastDao.insert(TestUtils.any())).thenAnswer {
            val broadcast: BroadcastEntity = it.getArgument(0)
            scrapedBroadcasts.add(broadcast)
        }
    }

    @Test
    fun scrapeShowDetails_fromFile() {
        expectedShows = MockObjects.showDetails

        expectedShows.forEach { show ->
            val id = show.id
            val html = TestUtils.loadFromResource("$id-show-details.html")
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

            assertEquals("Expected to find broadcastsLiveData", expectedBroadcastsForShow, scrapedBroadcastsForShow)
        }
    }
}