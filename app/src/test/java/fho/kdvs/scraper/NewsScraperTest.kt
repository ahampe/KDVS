package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.NewsEntity
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class NewsScraperTest : ScraperTest() {
    private val scrapedArticles = mutableListOf<NewsEntity>()
    private lateinit var expectedArticles: List<NewsEntity>

    @Before
    override fun setup() {
        super.setup()

        `when`(newsDao.insert(TestUtils.any())).thenAnswer{
            val article: NewsEntity = it.getArgument(0)
            scrapedArticles.add(article)
        }
    }

    @Test
    fun scrapeNews_fromFile() {
        expectedArticles = MockObjects.news

        val html = TestUtils.loadFromResource("News.html")
        scraperManager.scrapeNews(html)

        expectedArticles.forEach { article ->
            assertTrue("Expected to find article ${article.title}",
                scrapedArticles.contains(article))
        }
    }
}