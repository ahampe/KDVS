package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.StaffEntity
import fho.kdvs.global.extensions.fromHtmlSafe
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StaffScraperTest : ScraperTest() {
    private val scrapedStaff = mutableListOf<StaffEntity>()
    private lateinit var expectedStaff: List<StaffEntity>

    @Before
    override fun setup() {
        super.setup()

        every { staffDao.insert(any()) } answers {
            val staffMember = firstArg() as StaffEntity
            scrapedStaff.add(staffMember)
        }

        every { staffDao.deleteAll() } answers {
            scrapedStaff.clear()
        }

        every {
            kdvsPreferences getProperty "lastStaffScrape"
        } nullablePropertyType Long::class answers {
            fieldValue
        }

        every {
            kdvsPreferences setProperty "lastStaffScrape" value any<Long>()
        } answers {
            value
        }

        // Because this extension method depends on a static method, we must mock it
        mockkStatic("fho.kdvs.global.extensions.StringKt")

        every { any<String>().fromHtmlSafe() } answers { firstArg() }
    }

    @Test
    fun scrapeStaff_fromFile() {
        expectedStaff = MockObjects.staffMembers

        val html = TestUtils.loadFromResource("Contact.html")
        scraperManager.scrapeStaff(html)

        expectedStaff.forEach { staff ->
            assertTrue(
                "Expected to find staff ${staff.name}",
                scrapedStaff.contains(staff)
            )
        }
    }
}