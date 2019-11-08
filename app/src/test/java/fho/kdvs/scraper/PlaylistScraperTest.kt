package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.util.TimeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class PlaylistScraperTest : ScraperTest() {
    private val scrapedTracks = mutableListOf<TrackEntity>()
    private lateinit var scrapedBroadcast: BroadcastEntity

    private lateinit var expectedTracks: List<TrackEntity>
    private lateinit var expectedBroadcast: BroadcastEntity

    @Before
    override fun setup() {
        super.setup()

        `when`(
            broadcastDao.updateBroadcastDetails(
                TestUtils.any(), TestUtils.any(), TestUtils.any()
            )
        ).thenAnswer {
            scrapedBroadcast.description = it.getArgument(1)
            scrapedBroadcast.imageHref = it.getArgument(2)
            assertEquals(scrapedBroadcast.broadcastId, it.getArgument(0))
        }

        `when`(trackDao.insert(TestUtils.any())).thenAnswer {
            val track: TrackEntity = it.getArgument(0)
            scrapedTracks.add(track)
        }
    }

    @Test
    fun scrapePlaylist_fromFile() {
        scrapedBroadcast = BroadcastEntity(
            broadcastId = 51742,
            showId = 5361,
            date = TimeHelper.makeLocalDate("2019-01-12")
        )

        expectedTracks = MockObjects.playlist
        expectedBroadcast = BroadcastEntity(
            broadcastId = 51742,
            showId = 5361,
            date = TimeHelper.makeLocalDate("2019-01-12"),
            description = "I opened the Prog basket and was delighted to find some of my liveFavorites prog ingredients. ELP, PFM, Triumvrat, and IQ will combine to get the project started. I will try to spice these up with new music from Dilemma and Eden in Progress. And, as usual, an Improbably Proggy trak to throw into the mix from Alice Cooper. Questions, comments and suggestions to rockshurewood@gmail.com.",
            imageHref = "http://www.sevenwondersofwashingtonstate.com/uploads/4/7/4/6/47460045/3719499_orig.jpg"
        )

        val html = TestUtils.loadFromResource("playlist-51742.html")
        scraperManager.scrapePlaylist(html)

        assertEquals("Should have updated broadcast", expectedBroadcast, scrapedBroadcast)

        val scrapedSongs = scrapedTracks.map { it.song }

        expectedTracks.forEach { track ->
            assertTrue(
                "Expected to find song name ${track.song}",
                scrapedSongs.contains(track.song)
            )
            assertTrue("Expected to find track $track", scrapedTracks.contains(track))
        }
    }

    @Test
    fun scrapeEmptyPlaylist_fromFile() {
        scrapedBroadcast = BroadcastEntity(broadcastId = 51013)

        val html = TestUtils.loadFromResource("playlist-51013.html")
        scraperManager.scrapePlaylist(html)

        assertTrue("Expected to have no scraped tracks", scrapedTracks.isEmpty())
    }
}