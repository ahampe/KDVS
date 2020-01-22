package fho.kdvs.scraper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.topmusic.TopMusicType
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TopMusicScraperTest : ScraperTest() {
    private val scrapedTopAdds = mutableListOf<TopMusicEntity>()
    private val scrapedTopAlbums = mutableListOf<TopMusicEntity>()
    private lateinit var expectedTopAdds: List<TopMusicEntity>
    private lateinit var expectedTopAlbums: List<TopMusicEntity>

    @Before
    override fun setup() {
        super.setup()

        whenever(topMusicDao.insert(any())).doAnswer {
            val topMusic = it.arguments[0] as TopMusicEntity
            when (topMusic.type) {
                TopMusicType.ADD -> {
                    scrapedTopAdds.add(topMusic)
                }
                TopMusicType.ALBUM -> {
                    scrapedTopAlbums.add(topMusic)
                }
                else -> {
                }
            }
            null
        }
    }

    @Test
    fun scrapeTopAdds_fromFile() {
        expectedTopAdds = MockObjects.topAdds

        val topAddsHtml = TestUtils.loadFromResource("Top5Adds.html")
        scraperManager.scrapeTopMusic(topAddsHtml)

        expectedTopAdds.forEach { add ->
            assertTrue(
                "Expected to find add ${add.artist} - ${add.album} at position ${add.position} for week of ${add.weekOf}",
                scrapedTopAdds.contains(add)
            )
        }
    }

    @Test
    fun scrapeTopAlbums_fromFile() {
        expectedTopAlbums = MockObjects.topAlbums

        val topAlbumsHtml = TestUtils.loadFromResource("Top30Albums.html")
        scraperManager.scrapeTopMusic(topAlbumsHtml)

        expectedTopAlbums.forEach { album ->
            assertTrue(
                "Expected to find album ${album.artist} - ${album.album} at position ${album.position} for week of ${album.weekOf}",
                scrapedTopAlbums.contains(album)
            )
        }
    }
}