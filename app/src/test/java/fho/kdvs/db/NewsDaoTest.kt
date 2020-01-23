package fho.kdvs.db

import fho.kdvs.MockObjects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NewsDaoTest : DatabaseTest() {
    private val newsDao by lazy { db.newsDao() }

    @Test
    fun insert_basic() {
        val newsItem = MockObjects.news.first()

        newsDao.insert(newsItem)

        val newsDb = db.newsDao().getAllNews()

        assertTrue(newsDb.contains(newsItem))
        assertEquals(1, newsDb.size)
    }

    @Test
    fun insert_multiple() {
        val newsItems = MockObjects.news

        newsItems.forEach {
            newsDao.insert(it)
        }

        val newsDb = db.newsDao().getAllNews()

        newsItems.forEach {
            assertTrue(newsDb.contains(it))
        }

        assertEquals(newsItems.size, newsDb.size)
    }

    @Test
    fun delete_news_by_title_and_date() {
        val newsItem = MockObjects.news.first()

        newsDao.insert(newsItem)
        newsDao.deleteByTitleAndDate(newsItem.title, newsItem.date)

        val newsDb = db.newsDao().getAllNews()

        assertEquals("delete news failed", 0, newsDb.count { n -> n.title == newsItem.title })
    }
}