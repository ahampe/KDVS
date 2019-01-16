package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class ShowScraperTest : ScraperTest() {
    private lateinit var scrapedShow: ShowEntity

    private val expectedShow = ShowEntity(
        id = 5326,
        name = "The Smiles Show",
        host = "Jimmy Smiles",
        genre = "Public Affairs",
        defaultDesc = "Things in life that bring us happiness, local events (wellness and physical activity), and interviews with local artisans."
    )

    @Before
    override fun setup() {
        super.setup()

        scrapedShow = ShowEntity(
            id = 5326,
            name = "The Smiles Show"
        )
    }

    @Test
    fun scrapeShow_fromFile() {
        val scrapedBroadcasts = mutableListOf<BroadcastEntity>()

        `when`(
            showDao.updateShowInfo( // ...
                TestUtils.any(), TestUtils.any(), TestUtils.any(), TestUtils.any()
            )
        ).thenAnswer {
            scrapedShow.host = it.getArgument(1)
            scrapedShow.genre = it.getArgument(2)
            scrapedShow.defaultDesc = it.getArgument(3)

            assert(scrapedShow.id == it.getArgument(0))
        }

        `when`(broadcastDao.insert(TestUtils.any())).thenAnswer {
            val broadcast: BroadcastEntity = it.getArgument(0)
            scrapedBroadcasts.add(broadcast)
        }

        val html = TestUtils.loadFromResource("show-5326.html")

        scraperManager.scrapeShow(html)

        assertEquals("Should have updated show", scrapedShow, expectedShow)

        val expectedBroadcasts = MockObjects.showBroadcasts

        val scrapedBrIds = scrapedBroadcasts.map { it.broadcastId }

        expectedBroadcasts.forEach { broadcast ->
            assertTrue(
                "Expected to find broadcast ${broadcast.broadcastId}",
                scrapedBrIds.contains(broadcast.broadcastId)
            )
            assertTrue("Expected to find broadcast", scrapedBroadcasts.contains(broadcast))
        }
    }
}