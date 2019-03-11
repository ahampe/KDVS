package fho.kdvs.show

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.ScheduleScrapeData
import fho.kdvs.global.web.WebScraperManager
import fho.kdvs.schedule.TimeSlot
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao,
    private val scraperManager: WebScraperManager,
    private val broadcastRepository: BroadcastRepository,
    private val kdvsPreferences: KdvsPreferences
) : CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val playingShowLiveData = MutableLiveData<ShowEntity>()
    val nextShowLiveData = MutableLiveData<ShowEntity>()

    /** Asynchronously updates the [playingShowLiveData] and [nextShowLiveData] based on the current time. */
    fun updateLiveShows(): Job = launch {
        val scheduleTime = TimeHelper.makeEpochRelativeTime(OffsetDateTime.now())

        // If we have the next show, we can use its value to update the live show
        val upcomingShow = nextShowLiveData.value
        val currentShow = (if (upcomingShow != null) {
            val nextShowStart = upcomingShow.timeStart ?: OffsetDateTime.MAX

            // If the next show hasn't started yet, don't update and exit early
            if (scheduleTime < nextShowStart) return@launch

            // Special case when it's Saturday night and the next show starts Sunday morning:
            if (scheduleTime.dayOfWeek == DayOfWeek.SATURDAY && nextShowStart.dayOfWeek == DayOfWeek.SUNDAY) return@launch

            // If we've reached this point, we can safely update the playingShow
            upcomingShow
        } else {
            // Have to determine current show from database
            getShowAtTime(scheduleTime)
        }) ?: return@launch

        playingShowLiveData.postValue(currentShow)
    }

    /** Call when refreshing or initializing schedule views to fetch the most recent data. */
    fun fetchShows() {
        // TODO check last scrape time (SharedPrefs)
        scraperManager.scrape(URLs.SCHEDULE)
    }

    /** Fetches a [LiveData] that will wrap the show matching the provided ID. */
    fun showById(showId: Int): LiveData<ShowEntity> =
        showDao.showById(showId)

    /**
     * Given the day of week, quarter, and year, finds all shows that begin or end on that day,
     * and transforms them into [TimeSlot]s.
     */
    fun getShowTimeSlotsForDay(day: Day, quarter: Quarter, year: Int): Flowable<List<TimeSlot>> {
        val (timeStart, timeEnd) = TimeHelper.makeDayRange(day)
        return showDao.allShowsInTimeRange(timeStart, timeEnd, quarter, year)
            .observeOn(Schedulers.io())
            .map { showsList ->
                showsList.groupBy { show -> Pair(show.timeStart, show.timeEnd) }
                    .map { map ->
                        val showGroup = map.value
                        val isFirstHalfOrEntireSegment = ((showGroup.firstOrNull()?.timeStart?.dayOfWeek
                                == showGroup.firstOrNull()?.timeEnd?.dayOfWeek)
                                || (showGroup.firstOrNull()?.timeStart?.dayOfWeek.toString().capitalize()
                                == day.toString().capitalize()))
                        TimeSlot(showGroup, isFirstHalfOrEntireSegment)
                    }
            }
    }

    private suspend fun getShowAtTime(time: OffsetDateTime): ShowEntity? {
        // ensure that the quarter and year we're using are up to date, as well as the shows
        val scrapeData = scraperManager.scrapeBlocking(URLs.SCHEDULE) as? ScheduleScrapeData ?: return null
        val quarter = scrapeData.quarterYear.quarter
        val year = scrapeData.quarterYear.year

        val allShowsAtTime = showDao.getShowsAtTime(time, quarter, year)
        if (allShowsAtTime.isEmpty()) return null

        if (allShowsAtTime.size == 1) {
            // easy case where only one show exists in this time slot
            return allShowsAtTime.first()
        }

        // If there are more than two shows in this TimeSlot, we need to find which one is scheduled this week.
        // First we need to scrape each show for the most recent broadcast, and wait for each to complete
        val jobs = mutableListOf<Job?>()
        allShowsAtTime.forEach { show ->
            jobs += broadcastRepository.fetchBroadcastsForShow(show.id.toString())
        }
        jobs.forEach { it?.join() }

        // Now get the most recent broadcasts, if they exist
        val latestBroadcasts = allShowsAtTime.mapNotNull { show ->
            broadcastRepository.getLatestBroadcastForShow(show.id)
        }

        return when (latestBroadcasts.size) {
            0 -> {
                // If we have no broadcasts to work with, just assume that one of the shows is the current one
                allShowsAtTime.first()
            }
            1 -> {
                // If only one show has broadcasts at this point, it is probably the current one
                allShowsAtTime.find { it.id == latestBroadcasts.first().showId }
            }
            else -> {
                // First attempt to match the broadcast dates with the current date
                // Even if the dates don't match exactly, they will be equal by (difference in weeks) % (number of shows in timeslot)
                val n = allShowsAtTime.size
                val now = LocalDate.now()

                val matchingBroadcast = latestBroadcasts.filter { it.date != null }
                    .find { ChronoUnit.WEEKS.between(it.date, now) % n == 0L }

                val matchingShow = allShowsAtTime.find { it.id == matchingBroadcast?.showId }

                matchingShow ?: allShowsAtTime.first()
            }
        }
    }
}