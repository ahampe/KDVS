package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.TopMusicEntity
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class TopMusicScraperTest : ScraperTest() {
    private val scrapedTopAdds = mutableListOf<TopMusicEntity>()
    private val scrapedTopAlbums = mutableListOf<TopMusicEntity>()
    private lateinit var expectedTopAdds: List<TopMusicEntity>
    private lateinit var expectedTopAlbums: List<TopMusicEntity>

    @Before
    override fun setup() {
        super.setup()

        `when`(topMusicDao.insert(TestUtils.any())).thenAnswer{
            val topMusic: TopMusicEntity = it.getArgument(0)
            if (topMusic.isNewAdd)
                scrapedTopAdds.add(topMusic)
            else
                scrapedTopAlbums.add(topMusic)
        }
    }

    @Test
    fun scrapeTopAdds_fromFile() {
        expectedTopAdds = MockObjects.topAdds

        val topAddsHtml = TestUtils.loadFromResource("Top5Adds.html")
        scraperManager.scrapeTopMusic(topAddsHtml)

        expectedTopAdds.forEach { add ->
            assertTrue("Expected to find add ${add.artist} - ${add.album} at position ${add.position} for week of ${add.weekOf}",
                scrapedTopAdds.contains(add))
        }
    }

    @Test
    fun scrapeTopAlbums_fromFile() {
        expectedTopAlbums = MockObjects.topAlbums

        val topAlbumsHtml = TestUtils.loadFromResource("Top30Albums.html")
        scraperManager.scrapeTopMusic(topAlbumsHtml)

        expectedTopAlbums.forEach { add ->
            assertTrue("Expected to find album ${add.artist} - ${add.album} at position ${add.position} for week of ${add.weekOf}",
                scrapedTopAlbums.contains(add))
        }
    }
}