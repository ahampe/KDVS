package fho.kdvs.scraper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.FundraiserEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FundraiserScraperTest : ScraperTest() {
    private val scrapedFundraiser = mutableListOf<FundraiserEntity>()

    private lateinit var expectedFundraiser: FundraiserEntity

    @Before
    override fun setup() {
        super.setup()

        whenever(fundraiserDao.insert(any())).doAnswer {
            val fundraiser = it.arguments[0] as FundraiserEntity
            scrapedFundraiser.add(fundraiser)
            null
        }
    }

    @Test
    fun scrapeFundraiser_fromFile() {
        expectedFundraiser = MockObjects.fundraiser

        val fundraiserHtml = TestUtils.loadFromResource("Fundraiser.html")
        scraperManager.scrapeFundraiser(fundraiserHtml)

        assertEquals(
            "Expected to find fundraiser starting on ${expectedFundraiser.dateStart}",
            expectedFundraiser, scrapedFundraiser.first()
        )
    }
}