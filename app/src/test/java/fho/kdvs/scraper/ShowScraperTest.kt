package fho.kdvs.scraper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
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

        whenever(
            showDao.updateShowDetails(any(), any(), any(), any())
        ).doAnswer {
            val show = ShowEntity(
                id = it.arguments[0] as Int,
                host = it.arguments[1] as String?,
                genre = it.arguments[2] as String?,
                defaultDesc = it.arguments[3] as String?
            )

            scrapedShow = show

            null
        }

        whenever(broadcastDao.insert(any())).doAnswer {
            val broadcast = it.arguments[0] as BroadcastEntity
            scrapedBroadcasts.add(broadcast)
            null
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

            assertEquals(
                "Expected to find broadcastsLiveData",
                expectedBroadcastsForShow,
                scrapedBroadcastsForShow
            )
        }
    }
}