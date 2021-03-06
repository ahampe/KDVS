package fho.kdvs.services

import android.app.AlarmManager.INTERVAL_DAY
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.database.ShowTimeslotEntity
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.show.ShowRepository
import kotlinx.coroutines.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * A helper class whose responsibility is the seemingly simple task of
 * calculating updates to whatever show / broadcast is playing live.
 */
@Singleton
class LiveShowUpdater @Inject constructor(
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val showDao: ShowDao
) : CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    /** Signals this class to continuously update the currently playing show/broadcast. */
    fun beginUpdating() {
        job.cancelChildren()

        launch {
            while (isActive) {
                Timber.d("Attempting to update current show")
                val updateSuccess = updateLiveShowsAsync().await()

                if (updateSuccess) {
                    showRepository.liveShowLiveData.value?.let { show ->
                        val nowEpochSeconds = TimeHelper.makeEpochRelativeTime(TimeHelper.getNow())
                            .toEpochSecond()

                        val timeEnd = show.timeEnd ?: return@let
                        val timeStart = show.timeStart ?: return@let

                        var endEpochSeconds = timeEnd.toEpochSecond()

                        // Special case for shows that start Saturday and end Sunday, move end forward by a week
                        if (timeStart.dayOfWeek == DayOfWeek.SATURDAY
                            && timeEnd.dayOfWeek == DayOfWeek.SUNDAY
                        ) endEpochSeconds += WEEK_IN_MILLIS

                        val minutesDifference = (endEpochSeconds - nowEpochSeconds) / 60L
                        Timber.d("Waiting $minutesDifference minutes")
                        val millis = (endEpochSeconds - nowEpochSeconds) * 1000L
                        delay(millis)
                    }
                    // Wait an extra 5 seconds for safety
                    delay(5_000L)
                } else {
                    // wait ten seconds until trying again
                    Timber.d("Failed to get live show. Trying again in 10 seconds...")
                    delay(10_000L)
                }
            }
        }
    }

    /** Signals this class to stop updating the currently playing show/broadcast. */
    fun stopUpdating() {
        job.cancelChildren()
    }

    /**
     * Asynchronously updates [liveShowLiveData][ShowRepository.liveShowLiveData] and
     * [nextShowLiveData][ShowRepository.nextShowLiveData] based on the current time.
     * Returns a [Deferred] boolean indicating whether the computation was successful or not.
     */
    private fun updateLiveShowsAsync(): Deferred<Boolean> = async {
        val scheduleTime = TimeHelper.makeEpochRelativeTime(TimeHelper.getNow())

        // If we have the next show, we can use its value to update the live show
        val oldNextShow = showRepository.nextShowLiveData.value
        val currentShow = (if (oldNextShow != null) {
            // If the next show hasn't started yet, don't update and exit early
            val nextShowStart = oldNextShow.timeStart ?: OffsetDateTime.MAX

            if (scheduleTime < nextShowStart ||
                // special case when it's Saturday night and the next show starts Sunday morning:
                (scheduleTime.dayOfWeek == DayOfWeek.SATURDAY && nextShowStart.dayOfWeek == DayOfWeek.SUNDAY)
            ) return@async true

            // Or if the next up show has already ended somehow, forget it and look in the database
            val nextShowEnd = oldNextShow.timeEnd ?: OffsetDateTime.MIN
            if (scheduleTime > nextShowEnd ||
                // special case when it's Sunday morning and the next show ended Saturday night:
                (scheduleTime.dayOfWeek == DayOfWeek.SUNDAY && nextShowEnd.dayOfWeek == DayOfWeek.SATURDAY)
            ) {
                getShowTimeslotAtTime(scheduleTime)
            } else {
                // If we've reached this point, we can safely update the playingShow with the old next show
                oldNextShow
            }
        } else {
            // Have to determine current show from database
            getShowTimeslotAtTime(scheduleTime)
        }) ?: return@async false

        // the broadcast repository will take care of getting the live broadcast:
        broadcastRepository.updateLiveBroadcast(currentShow.id)

        showRepository.liveShowLiveData.postValue(currentShow)

        // upon starting the app, bind playerBar with live broadcast
        broadcastRepository.nowPlayingShowLiveData.postValue(currentShow)
        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcastRepository.liveBroadcastLiveData.value)
        // TODO: if user was last playing an archive stream, load that stream and its progress instead

        // to get the next show, we need the database and
        val addedTime = currentShow.timeEnd?.plusMinutes(1L) ?: return@async false
        // addedTime will most likely be within the epoch week, but it isn't guaranteed:
        val nextShowTime = TimeHelper.makeEpochRelativeTime(addedTime)

        val nextShow = getShowTimeslotAtTime(nextShowTime) ?: return@async false

        val subtractedTime = currentShow.timeStart?.minusMinutes(1L) ?: return@async false
        val previousShowTime = TimeHelper.makeEpochRelativeTime(subtractedTime)

        val previousShow = getShowTimeslotAtTime(previousShowTime) ?: return@async false

        showRepository.nextShowLiveData.postValue(nextShow)
        showRepository.previousShowLiveData.postValue(previousShow)

        return@async true
    }

    private suspend fun getAllShowTimeslotsAtTime(time: OffsetDateTime): List<ShowTimeslotEntity> {
        // ensure that the quarter and year we're using are up to date, as well as the shows
        showRepository.scrapeSchedule().join()
        val quarterYear = showDao.getCurrentQuarterYear()
            ?: return mutableListOf()
        val (quarter, year) = quarterYear

        return showDao.getShowTimeslotsAtTime(time, quarter, year).distinct()
    }

    private suspend fun getLatestBroadcastsForShowsAtTime(allShowsAtTime: List<ShowTimeslotEntity>): List<BroadcastEntity> {
        // If there are more than two shows in this TimeSlot, we need to find which one is scheduled this week.
        // First we need to scrape each show for the most recent broadcast, and wait for each to complete
        val jobs = mutableListOf<Job>()
        allShowsAtTime.forEach { show ->
            jobs += broadcastRepository.scrapeShow(show.id.toString())
        }
        jobs.forEach { it.join() }

        // Now get the most recent broadcasts, if they exist
        return allShowsAtTime.mapNotNull { show ->
            broadcastRepository.getLatestBroadcastForShow(show.id)
        }
    }

    /** A suspending function that will attempt to find a [ShowTimeslotEntity] at the given [time]. */
    private suspend fun getShowTimeslotAtTime(time: OffsetDateTime): ShowTimeslotEntity? {
        val allShowsAtTime = getAllShowTimeslotsAtTime(time)

        if (allShowsAtTime.isEmpty()) return null

        if (allShowsAtTime.size == 1) {
            // easy case where only one show exists in this time slot
            return allShowsAtTime.first()
        }

        val latestBroadcasts = getLatestBroadcastsForShowsAtTime(allShowsAtTime)

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
                // Even if the dates don't match exactly,
                // they will be equal by (difference in weeks) % (number of shows in timeslot)
                val n = allShowsAtTime.size
                val now = TimeHelper.getLocalNow()

                val matchingBroadcast = latestBroadcasts.filter { it.date != null }
                    .find { ChronoUnit.WEEKS.between(it.date, now) % n == 0L }

                val matchingShow = allShowsAtTime.find { it.id == matchingBroadcast?.showId }

                matchingShow ?: allShowsAtTime.first()
            }
        }
    }

    /**
     * Takes a time, returns [ShowTimeslotEntity]s at this time in the order in which they are
     * scheduled to air from current week. Defaults to the order returned by database.
     * Note: a show having already aired this week (or currently airing) will still be first.
     * */
    suspend fun orderShowTimeslotsAtTimeRelativeToCurrentWeekAsync(timeStart: OffsetDateTime): List<ShowTimeslotEntity> {
        Timber.d("Ordering shows in timeslot at $timeStart")

        val showsAtTime = getAllShowTimeslotsAtTime(timeStart)

        val latestBroadcasts = getLatestBroadcastsForShowsAtTime(showsAtTime)

        if (latestBroadcasts.isNotEmpty() &&
            latestBroadcasts.size >= (showsAtTime.size - 1)
        ) {

            val orderedShows = mutableListOf<ShowTimeslotEntity>()

            showsAtTime.forEachIndexed { i, _ ->
                val now = LocalDate.now()

                val matchingBroadcast = latestBroadcasts
                    .filter { it.date != null }
                    .find {
                        ChronoUnit.WEEKS.between(it.date, now) % showsAtTime.size == i.toLong()
                    }

                val matchingShow = showsAtTime.find {
                    it.id == matchingBroadcast?.showId
                }

                matchingShow?.let {
                    orderedShows.add(matchingShow)
                }
            }

            // if scraped broadcasts do not include show S, assume S to be this week's show
            if (latestBroadcasts.size == (showsAtTime.size - 1)) {
                orderedShows.add(0, showsAtTime.first { s -> !orderedShows.contains(s) })
            }

            return orderedShows
        } else {
            return showsAtTime
        }
    }

    companion object {
        const val WEEK_IN_MILLIS = 7L * INTERVAL_DAY
    }
}