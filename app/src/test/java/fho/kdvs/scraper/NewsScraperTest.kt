package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.TestUtils.isEqualIgnoringProperties
import fho.kdvs.global.database.NewsEntity
import io.mockk.every
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NewsScraperTest : ScraperTest() {
    private val scrapedArticles = mutableListOf<NewsEntity>()
    private lateinit var expectedArticles: List<NewsEntity>

    @Before
    override fun setup() {
        super.setup()

        every { newsDao.insert(any()) } answers {
            val article = firstArg() as NewsEntity
            scrapedArticles.add(article)
        }

        every {
            kdvsPreferences getProperty "lastNewsScrape"
        } nullablePropertyType Long::class answers {
            fieldValue
        }

        every {
            kdvsPreferences setProperty "lastNewsScrape" value any<Long>()
        } answers {
            value
        }
    }

    @Test
    fun scrapeNews_fromFile() {
        expectedArticles = MockObjects.news

        val html = TestUtils.loadFromResource("News.html")
        scraperManager.scrapeNews(html)

        expectedArticles.forEach { article ->
            assertTrue(
                "Expected to find article ${article.title}",

                scrapedArticles.any {
                    article.isEqualIgnoringProperties(it, listOf(NewsEntity::newsId))
                }
            )
        }
    }
}