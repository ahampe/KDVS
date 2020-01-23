package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.TestUtils.isEqualIgnoringProperties
import fho.kdvs.global.database.FundraiserEntity
import io.mockk.every
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FundraiserScraperTest : ScraperTest() {
    private val scrapedFundraiser = mutableListOf<FundraiserEntity>()

    private lateinit var expectedFundraiser: FundraiserEntity

    @Before
    override fun setup() {
        super.setup()

        every { fundraiserDao.insert(any()) } answers {
            val fundraiser = firstArg<FundraiserEntity>()
            scrapedFundraiser.add(fundraiser)
        }

        every { fundraiserDao.deleteAll() } answers {
            scrapedFundraiser.clear()
        }

        every {
            kdvsPreferences getProperty "lastFundraiserScrape"
        } nullablePropertyType Long::class answers {
            fieldValue
        }

        every {
            kdvsPreferences setProperty "lastFundraiserScrape" value any<Long>()
        } answers {
            value
        }
    }

    @Test
    fun scrapeFundraiser_fromFile() {
        expectedFundraiser = MockObjects.fundraiser

        val fundraiserHtml = TestUtils.loadFromResource("Fundraiser.html")
        scraperManager.scrapeFundraiser(fundraiserHtml)

        val fundraiser = scrapedFundraiser.first()

        assertTrue(
            "Expected to find fundraiser starting on ${expectedFundraiser.dateStart}",
            fundraiser.isEqualIgnoringProperties(expectedFundraiser, listOf(FundraiserEntity::fundraiserId))
        )
    }
}