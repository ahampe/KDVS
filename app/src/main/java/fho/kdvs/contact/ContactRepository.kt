package fho.kdvs.show

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.ContactDao
import fho.kdvs.global.database.ContactEntity
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val scraperManager: WebScraperManager,
    private val kdvsPreferences: KdvsPreferences
) : BaseRepository() {

    // TODO: Make this quarterly?
    /** Runs a contact scrape if it hasn't been fetched recently. */
    fun scrapeContact() = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.lastContactsScrape ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeContact()?.join()
        } else {
            Timber.d("News has already been scraped recently; skipping")
        }
    }

    /**
     * Runs a contacts scrape without checking when it was last performed.
     * The only acceptable public usage of this method is when user explicitly refreshes.
     */
    private fun forceScrapeContact(): Job? = scraperManager.scrape(URLs.CONTACT)

    fun getContacts(): LiveData<List<ContactEntity>> =
        contactDao.getAll()
}