package fho.kdvs.services

import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.schedule.TimeSlot
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
                        val nowEpochSeconds = TimeHelper.makeEpochRelativeTime(OffsetDateTime.now())
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
        val scheduleTime = TimeHelper.makeEpochRelativeTime(OffsetDateTime.now())

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
                getShowAtTime(scheduleTime)
            } else {
                // If we've reached this point, we can safely update the playingShow with the old next show
                oldNextShow
            }
        } else {
            // Have to determine current show from database
            getShowAtTime(scheduleTime)
        }) ?: return@async false

        // the broadcast repository will take care of getting the live broadcast:
        broadcastRepository.updateLiveBroadcast(currentShow.id)

        showRepository.liveShowLiveData.postValue(currentShow)

        // upon starting the app, bind playerBar with live show if repo doesn't already have value
        if (broadcastRepository.nowPlayingShowLiveData.value == null) {
            showRepository.liveShowLiveData.postValue(currentShow)
            broadcastRepository.nowPlayingShowLiveData.postValue(currentShow)
        }

        // to get the next show, we need the database and
        val addedTime = currentShow.timeEnd?.plusMinutes(1L) ?: return@async false
        // addedTime will most likely be within the epoch week, but it isn't guaranteed:
        val nextShowTime = TimeHelper.makeEpochRelativeTime(addedTime)

        val nextShow = getShowAtTime(nextShowTime) ?: return@async false

        showRepository.nextShowLiveData.postValue(nextShow)

        return@async true
    }

    private suspend fun getAllShowsAtTime(time: OffsetDateTime): List<ShowEntity> {
        // ensure that the quarter and year we're using are up to date, as well as the shows
        showRepository.scrapeSchedule().join()
        val quarterYear = showDao.getCurrentQuarterYear()
            ?: return mutableListOf()
        val (quarter, year) = quarterYear

        return showDao.getShowsAtTime(time, quarter, year)
    }

    private suspend fun getLatestBroadcastsForShowsAtTime(allShowsAtTime: List<ShowEntity>) : List<BroadcastEntity>{
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

    /** A suspending function that will attempt to find a [ShowEntity] at the given [time]. */
    private suspend fun getShowAtTime(time: OffsetDateTime): ShowEntity? {
        val allShowsAtTime = getAllShowsAtTime(time)

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
                val now = LocalDate.now()

                val matchingBroadcast = latestBroadcasts.filter { it.date != null }
                    .find { ChronoUnit.WEEKS.between(it.date, now) % n == 0L }

                val matchingShow = allShowsAtTime.find { it.id == matchingBroadcast?.showId }

                matchingShow ?: allShowsAtTime.first()
            }
        }
    }

    /** Takes a timeslot, returns its shows in the order in which they are scheduled to air from current time.*/
    fun orderShowsInTimeSlotRelativeToCurrentWeek(timeslot: TimeSlot): List<ShowEntity?> {
        job.cancelChildren()

        val orderedShows = mutableListOf<ShowEntity?>()

        launch {
            if (isActive) {
                Timber.d("Attempting to order shows in timeslot by their relative order of appearance")
                val allShowsAtTime = getAllShowsAtTime(timeslot.timeStart!!)
                val latestBroadcasts = getLatestBroadcastsForShowsAtTime(allShowsAtTime)

                if (timeslot.ids.count() > 0 && latestBroadcasts.isNotEmpty() &&
                    latestBroadcasts.size >= (timeslot.ids.count() - 1)) {
                    timeslot.ids.forEachIndexed { i, _ ->
                        val n = allShowsAtTime.size
                        val now = LocalDate.now()
                        val matchingBroadcast = latestBroadcasts.filter { it.date != null }
                            .find { ChronoUnit.WEEKS.between(it.date, now) % n == i.toLong() }
                        val matchingShow = allShowsAtTime.find { it.id == matchingBroadcast?.showId }

                        orderedShows.add(matchingShow)
                    }
                }
            }
        }

        return orderedShows
    }

    companion object {
        private const val WEEK_IN_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}