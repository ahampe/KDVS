package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.TestUtils.isEqualIgnoringProperties
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.topmusic.TopMusicType
import io.mockk.every
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

        every { topMusicDao.insert(any()) } answers {
            val topMusic = firstArg() as TopMusicEntity
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
        }

        every {
            kdvsPreferences getProperty "lastTopAddsScrape"
        } nullablePropertyType Long::class answers {
            fieldValue
        }

        every {
            kdvsPreferences setProperty "lastTopAddsScrape" value any<Long>()
        } answers {
            value
        }

        every {
            kdvsPreferences getProperty "lastTopAlbumsScrape"
        } nullablePropertyType Long::class answers {
            fieldValue
        }

        every {
            kdvsPreferences setProperty "lastTopAlbumsScrape" value any<Long>()
        } answers {
            value
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

                scrapedTopAdds.any {
                    add.isEqualIgnoringProperties(it, listOf(TopMusicEntity::topMusicId))
                }
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
                scrapedTopAlbums.any {
                    album.isEqualIgnoringProperties(it, listOf(TopMusicEntity::topMusicId))
                }
            )
        }
    }
}