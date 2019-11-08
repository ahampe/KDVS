package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.FundraiserEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class FundraiserScraperTest : ScraperTest() {
    private val scrapedFundraiser = mutableListOf<FundraiserEntity>()

    private lateinit var expectedFundraiser: FundraiserEntity

    @Before
    override fun setup() {
        super.setup()

        `when`(fundraiserDao.insert(TestUtils.any())).thenAnswer {
            val fundraiser: FundraiserEntity = it.getArgument(0)
            scrapedFundraiser.add(fundraiser)
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