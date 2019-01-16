package fho.kdvs

import fho.kdvs.TestUtils.loadFromResource
import fho.kdvs.model.database.KdvsDatabase
import fho.kdvs.model.database.daos.BroadcastDao
import fho.kdvs.model.database.daos.ShowDao
import fho.kdvs.model.database.daos.TrackDao
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
import fho.kdvs.model.web.WebScraperManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

open class ScraperTest {
    private lateinit var scraperManager: WebScraperManager

    private lateinit var db: KdvsDatabase
    private lateinit var showDao: ShowDao
    private lateinit var broadcastDao: BroadcastDao
    private lateinit var trackDao: TrackDao

    @Before
    open fun setup() {
        db = mock(KdvsDatabase::class.java)
        showDao = mock(ShowDao::class.java)
        `when`(db.showDao()).thenReturn(showDao)
        broadcastDao = mock(BroadcastDao::class.java)
        `when`(db.broadcastDao()).thenReturn(broadcastDao)
        trackDao = mock(TrackDao::class.java)
        `when`(db.trackDao()).thenReturn(trackDao)

        scraperManager = WebScraperManager(db)
    }

    @Test
    fun scrapeSchedule_fromFile() {
        val scrapedShows = mutableListOf<ShowEntity>()

        `when`(showDao.insert(TestUtils.any())).thenAnswer {
            val show: ShowEntity = it.getArgument(0)
            scrapedShows.add(show)
        }

        val html = loadFromResource("schedule-grid.html")

        scraperManager.scrapeSchedule(html)

        val scrapedNames = scrapedShows.map { it.name }

        val compareShows = MockObjects.scheduleShows

        compareShows.forEach { show ->
            assertTrue("Expected to find show with name ${show.name}", scrapedNames.contains(show.name))
            assertTrue("Expected to find show with details $show", scrapedShows.contains(show))
        }

        assertEquals(compareShows.size, scrapedShows.size)
    }

    @Test
    fun scrapeShowDetails_fromFile() {
        val scrapedShows = mutableListOf<ShowEntity>()
        val showIds = listOf(5235, 5238, 5239, 5240, 5257, 5280, 5289, 5320, 5331, 5333, 5355, 5364, 5370)

        `when`(showDao.updateShowInfo(TestUtils.any(),TestUtils.any(),TestUtils.any(),TestUtils.any())).thenAnswer {
            var show = ShowEntity(it.getArgument(0),
                null,
                it.getArgument(1),
                it.getArgument(2),
                it.getArgument(3))
            scrapedShows.add(show)
        }

        val compareShows = MockObjects.showDetails

        showIds.forEach{
            val html = loadFromResource(it.toString() + "-show-details.html")

            scraperManager.scrapeShow(html)

            compareShows.filter { s -> s.id == it }
            .forEach { show ->
                assertTrue("Expected to find show ${show.id} with details $show", scrapedShows.contains(show))
            }
        }
    }

    @Test
    fun scrapeBroadcasts_fromFile() {
        val scrapedBroadcasts = mutableListOf<BroadcastEntity>()
        val showIds = listOf(5280, 5240)

        `when`(showDao.updateShowInfo(TestUtils.any(),TestUtils.any(),TestUtils.any(),TestUtils.any())).thenAnswer {}
        `when`(broadcastDao.insert(TestUtils.any())).thenAnswer {
            var broadcast: BroadcastEntity = it.getArgument(0)
            scrapedBroadcasts.add(broadcast)
        }

        val compareBroadcasts = MockObjects.broadcasts

        showIds.forEach{
            val html = loadFromResource(it.toString() + "-show-details.html")

            scraperManager.scrapeShow(html)

            compareBroadcasts.filter { b -> b.showId == it }
            .forEach { broadcast ->
                assertTrue("Expected to find show ${broadcast.broadcastId} with details $broadcast", scrapedBroadcasts.contains(broadcast))
            }
        }

        assertEquals(compareBroadcasts.size, scrapedBroadcasts.size)
    }

    @Test
    fun scrapeBroadcastDetails_fromFile() {
        val scrapedBroadcasts = mutableListOf<BroadcastEntity>()
        val broadcastIds = listOf(50506, 50771, 51695)

        `when`(broadcastDao.updateBroadcast(TestUtils.any(),TestUtils.any(),TestUtils.any())).thenAnswer {
            var broadcast = BroadcastEntity(it.getArgument(0),
                null,
                it.getArgument(1),
                null,
                it.getArgument(2))
            scrapedBroadcasts.add(broadcast)
        }

        `when`(trackDao.insert(TestUtils.any())).thenAnswer {}

        val compareBroadcasts = MockObjects.broadcastDetails

        broadcastIds.forEach{
            val html = loadFromResource(it.toString() + "-playlist-details.html")

            scraperManager.scrapeBroadcast(html)

            compareBroadcasts.filter { b -> b.broadcastId == it }
            .forEach { broadcast ->
                assertTrue("Expected to find broadcast ${broadcast.broadcastId} with details $broadcast", scrapedBroadcasts.contains(broadcast))
            }
        }
    }

    @Test
    fun scrapePlaylist_fromFile() {
        val scrapedTracks = mutableListOf<TrackEntity>()
        val broadcastIds = listOf(50771, 50506)

        `when`(broadcastDao.updateBroadcast(TestUtils.any(),TestUtils.any(),TestUtils.any())).thenAnswer {}

        `when`(trackDao.insert(TestUtils.any())).thenAnswer {
            var track: TrackEntity = it.getArgument(0)
            scrapedTracks.add(track)
        }

        val compareTracks = MockObjects.trackDetails

        broadcastIds.forEach{
            val html = loadFromResource(it.toString() + "-playlist-details.html")

            scraperManager.scrapeBroadcast(html)

            compareTracks.forEach { track ->
                assertTrue("Expected to find track ${track.song} with details $track", scrapedTracks.contains(track))
            }
        }

        assertEquals(compareTracks.size, scrapedTracks.size)
    }
}