package fho.kdvs.model.repository

import androidx.lifecycle.LiveData
import fho.kdvs.extensions.toLiveData
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.web.WebScraperManager
import fho.kdvs.model.database.daos.ShowDao
import fho.kdvs.util.TimeHelper
import fho.kdvs.util.URLs
import io.reactivex.Flowable
import org.threeten.bp.Month
import org.threeten.bp.OffsetDateTime

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao,
    private val scraperManager: WebScraperManager
) {
    fun fetchShows() {
        // TODO check last scrape time (SharedPrefs)
        scraperManager.scrape(URLs.SCHEDULE)
    }

    fun getShowAtTime(time: OffsetDateTime): ShowEntity {
        //TODO: get quarter programmatically
        val quarter = when (time.month) {
            Month.JANUARY -> Quarter.WINTER
            Month.FEBRUARY -> Quarter.WINTER
            Month.MARCH -> Quarter.WINTER
            Month.APRIL -> Quarter.SPRING
            Month.MAY -> Quarter.SPRING
            Month.JUNE -> Quarter.SPRING
            Month.JULY -> Quarter.SUMMER
            Month.AUGUST -> Quarter.SUMMER
            Month.SEPTEMBER -> Quarter.SUMMER
            Month.OCTOBER -> Quarter.FALL
            Month.NOVEMBER -> Quarter.FALL
            Month.DECEMBER -> Quarter.FALL
        }

        val epochTime = TimeHelper.makeEpochRelativeTime(time)

        return showDao.getShowAtTime(epochTime, quarter, time.year)
    }

    fun getShowsForDay(day: Day, quarter: Quarter, year: Int): Flowable<List<ShowEntity>> {
        val (timeStart, timeEnd) = TimeHelper.makeDayRange(day)
        return showDao.allShowsInTimeRange(timeStart, timeEnd, quarter, year)
    }
}