package fho.kdvs

import fho.kdvs.database.models.Day
import fho.kdvs.web.StandardWebScraperFactory
import org.junit.Test

//import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//class ExampleUnitTest {
//    @Test
//    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
//    }
//}

class WebScraperTest  {
    @Test
    fun scrape() {
        val factory = StandardWebScraperFactory()
//        val scraper = factory.callFromUrl("https://kdvs.org/programming/schedule-grid/")
//        val scraper = factory.callFromUrl("https://kdvs.org/past-playlists/5106/")
        val scraper = factory.callFromUrl("https://kdvs.org/playlist-details/49575/")
        scraper.scrape()
    }

    @Test
    fun day() {
        assert(Day.valueOf("SUNDAY") == Day.SUNDAY)
    }
}


