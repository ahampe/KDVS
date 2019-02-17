package fho.kdvs.show

import androidx.lifecycle.LiveData
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import fho.kdvs.schedule.TimeSlot
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao,
    private val scraperManager: WebScraperManager,
    private val kdvsPreferences: KdvsPreferences
) {
    /** Fetches a [LiveData] that will wrap the show matching the provided ID. */
    fun showById(showId: Int): LiveData<ShowEntity> =
        showDao.showById(showId)

    /** Call when refreshing or initializing schedule views to fetch the most recent data. */
    fun fetchShows() {
        // TODO check last scrape time (SharedPrefs)
        scraperManager.scrape(URLs.SCHEDULE)
    }

    /**
     * Given the day of week, quarter, and year, finds all shows that begin or end on that day,
     * and transforms them into [TimeSlot]s.
     */
    fun getShowTimeSlotsForDay(day: Day, quarter: Quarter, year: Int): Flowable<List<TimeSlot>> {
        val (timeStart, timeEnd) = TimeHelper.makeDayRange(day)
        return showDao.allShowsInTimeRange(timeStart, timeEnd, quarter, year)
            .observeOn(Schedulers.io())
            .map { showsList ->
                showsList
                    .groupBy { show -> Pair(show.timeStart, show.timeEnd) }
                    .map { map -> map.value }
                    .map { showGroup -> TimeSlot(showGroup) }
            }
    }
}