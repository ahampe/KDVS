package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.StaffEntity
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class StaffScraperTest : ScraperTest() {
    private val scrapedStaff = mutableListOf<StaffEntity>()
    private lateinit var expectedStaff: List<StaffEntity>

    @Before
    override fun setup() {
        super.setup()

        `when`(staffDao.insert(TestUtils.any())).thenAnswer {
            val staffMember: StaffEntity = it.getArgument(0)
            scrapedStaff.add(staffMember)
        }
    }

    @Test
    fun scrapeStaff_fromFile() {
        expectedStaff = MockObjects.staffMembers

        val html = TestUtils.loadFromResource("Contact.html")
        scraperManager.scrapeStaff(html)

        expectedStaff.forEach { staff ->
            assertTrue("Expected to find staff with name ${staff.name}",
                scrapedStaff.map { s -> s.name }.contains(staff.name)
            )
            assertTrue("Expected to find staff with position ${staff.position}",
                scrapedStaff.map { s -> s.position }.contains(staff.position)
            )
            assertTrue("Expected to find staff with officeHours ${staff.officeHours}",
                scrapedStaff.map { s -> s.officeHours }.contains(staff.officeHours)
            )
            assertTrue("Expected to find staff with duties ${staff.duties}",
                scrapedStaff.map { s -> s.duties }.contains(staff.duties)
            )
            assertTrue("Expected to find staff with email ${staff.email}",
                scrapedStaff.map { s -> s.email }.contains(staff.email)
            )
        }
    }
}