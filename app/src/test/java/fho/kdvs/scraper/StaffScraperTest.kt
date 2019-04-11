package fho.kdvs.scraper

import fho.kdvs.MockObjects
import fho.kdvs.TestUtils
import fho.kdvs.global.database.StaffEntity
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class ContactScraperTest : ScraperTest() {
    private val scrapedContacts = mutableListOf<StaffEntity>()
    private lateinit var expectedStaff: List<StaffEntity>

    @Before
    override fun setup() {
        super.setup()

        `when`(staffDao.insert(TestUtils.any())).thenAnswer{
            val staffMember: StaffEntity = it.getArgument(0)
            scrapedContacts.add(staffMember)
        }
    }

    @Test
    fun scrapeContacts_fromFile() {
        expectedStaff = MockObjects.staffMembers

        val html = TestUtils.loadFromResource("Contact.html")
        scraperManager.scrapeContacts(html)

        expectedStaff.forEach { contact ->
            assertTrue("Expected to find contact with name ${contact.name}",
                scrapedContacts.map{c -> c.name}.contains(contact.name))
            assertTrue("Expected to find contact with position ${contact.position}",
                scrapedContacts.map{c -> c.position}.contains(contact.position))
            assertTrue("Expected to find contact with officeHours ${contact.officeHours}",
                scrapedContacts.map{c -> c.officeHours}.contains(contact.officeHours))
            assertTrue("Expected to find contact with duties ${contact.duties}",
                scrapedContacts.map{c -> c.duties}.contains(contact.duties))
            assertTrue("Expected to find contact with email ${contact.email}",
                scrapedContacts.map{c -> c.email}.contains(contact.email))
        }
    }
}