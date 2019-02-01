package fho.kdvs.model.repository

import androidx.lifecycle.LiveData
import fho.kdvs.extensions.toLiveData
import fho.kdvs.model.Day
import fho.kdvs.model.database.daos.ShowDao
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.web.WebScraperManager
import fho.kdvs.util.URLs
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao,
    private val scraperManager: WebScraperManager
) {

    val allShows: LiveData<List<ShowEntity>> by lazy {
        showDao.allShows().toLiveData()
    }

    fun fetchShows() {
        // TODO check last scrape time (SharedPrefs)
        scraperManager.scrape(URLs.SCHEDULE)
    }

    fun getShowsForDay(day: Day): Flowable<List<ShowEntity>> = showDao.allShowsForDay(day)
}