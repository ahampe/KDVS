package fho.kdvs

import fho.kdvs.web.StandardWebScraperFactory
import junit.framework.Assert.assertNotNull
import org.junit.Test

open class ScraperTest {
    @Test
    fun scrapeSchedule() {
        val factory = StandardWebScraperFactory()
        val scraper = factory.callFromUrl("https://kdvs.org/programming/schedule-grid/")
        scraper.scrape()

        assertNotNull(scraper.addList)
        assertNotNull(scraper.updateList)
    }
    @Test
    fun scrapeShow() {
        val factory = StandardWebScraperFactory()
        val scraper = factory.callFromUrl("https://kdvs.org/past-playlists/5163/")
        scraper.scrape()

        assertNotNull(scraper.addList)
        assertNotNull(scraper.updateList)
    }
    @Test
    fun scrapePlaylist() {
        val factory = StandardWebScraperFactory()
        var scraper = factory.callFromUrl("https://kdvs.org/playlist-details/49755/")
        scraper.scrape()

        assertNotNull(scraper.addList)
        assertNotNull(scraper.updateList)
    }

    @Test
    fun scrapeUpdatePlaylist() {
        val factory = StandardWebScraperFactory()
        var scraper = factory.callFromUrl("https://kdvs.org/playlist-details/49755/", false)
        scraper.scrape()

        assertNotNull(scraper.addList)
        assertNotNull(scraper.updateList)
    }
}