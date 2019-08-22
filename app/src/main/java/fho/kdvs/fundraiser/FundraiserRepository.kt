package fho.kdvs.fundraiser

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.FundraiserDao
import fho.kdvs.global.database.FundraiserEntity
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundraiserRepository @Inject constructor(
    private val fundraiserDao: FundraiserDao,
    private val scraperManager: WebScraperManager,
    private val kdvsPreferences: KdvsPreferences
) : BaseRepository() {

    /** Runs a fundraiser scrape if it hasn't been fetched recently. */
    fun scrapeFundraiser() = launch {
        val now = TimeHelper.getNow().toEpochSecond()
        val lastScrape = kdvsPreferences.lastFundraiserScraper ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeFundraiser()?.join()
        } else {
            Timber.d("Fundraiser has already been scraped recently; skipping")
        }
    }

    /**
     * Runs a fundraiser scrape without checking when it was last performed.
     * The only acceptable public usage of this method is when user explicitly refreshes.
     */
    private fun forceScrapeFundraiser(): Job? = scraperManager.scrape(URLs.FUNDRAISER)

    fun getFundraiser(): LiveData<FundraiserEntity> =
        fundraiserDao.get()
}