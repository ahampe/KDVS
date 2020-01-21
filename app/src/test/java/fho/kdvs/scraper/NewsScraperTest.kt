package fho.kdvs.scraper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.NewsEntity
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NewsScraperTest : ScraperTest() {
    private val scrapedArticles = mutableListOf<NewsEntity>()
    private lateinit var expectedArticles: List<NewsEntity>

    @Before
    override fun setup() {
        super.setup()

        whenever(newsDao.insert(any())).doAnswer {
            val article = it.arguments[0] as NewsEntity
            scrapedArticles.add(article)
            null
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
                scrapedArticles.contains(article)
            )
        }
    }
}