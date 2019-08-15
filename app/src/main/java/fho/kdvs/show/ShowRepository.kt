package fho.kdvs.show

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import fho.kdvs.schedule.QuarterYear
import fho.kdvs.schedule.TimeSlot
import fho.kdvs.services.KdvsPlaybackPreparer
import fho.kdvs.services.LiveShowUpdater
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao,
    private val scraperManager: WebScraperManager,
    private val broadcastRepository: BroadcastRepository,
    private val playbackPreparer: KdvsPlaybackPreparer,
    private val kdvsPreferences: KdvsPreferences
) : BaseRepository() {

    /**
     * [MutableLiveData] listening for the live show (not necessarily the currently playing show).
     * Whenever the value is set, a request to scrape its details is sent to [BroadcastRepository].
     */
    val liveShowLiveData = object : MutableLiveData<ShowEntity>() {
        override fun setValue(value: ShowEntity?) {
            value?.let { broadcastRepository.scrapeShow(it.id.toString()) }
            super.setValue(value)
        }
    }

    /** [MutableLiveData] listening for the show immediately preceding the current one. */
    val previousShowLiveData = MutableLiveData<ShowEntity>()

    /** [MutableLiveData] listening for the show immediately following the current one. */
    val nextShowLiveData = MutableLiveData<ShowEntity>()

    /** A [MediatorLiveData] which merges the previous show, live show, and next show. */
    val currentShowsLiveData = MediatorLiveData<List<ShowEntity>>()
        .apply {
            var previous: ShowEntity? = null
            var current: ShowEntity? = null
            var next: ShowEntity? = null

            addSource(previousShowLiveData) { prevShow ->
                previous = prevShow
                val currShow = current ?: return@addSource
                val nextShow = next ?: return@addSource
                postValue(listOf(prevShow, currShow, nextShow))
            }

            addSource(liveShowLiveData) { currShow ->
                current = currShow
                val prevShow = previous ?: return@addSource
                val nextShow = next ?: return@addSource
                postValue(listOf(prevShow, currShow, nextShow))
            }

            addSource(nextShowLiveData) { nextShow ->
                next = nextShow
                val prevShow = previous ?: return@addSource
                val currShow = current ?: return@addSource
                postValue(listOf(prevShow, currShow, nextShow))
            }
        }

    /** A [MediatorLiveData] which merges the live show and live broadcast. */
    val liveStreamLiveData = MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>()
        .apply {
            var broadcast: BroadcastEntity? = null
            var show: ShowEntity? = null

            addSource(liveShowLiveData) { showEntity ->
                show = showEntity
                postValue(Pair(showEntity, broadcast))
            }

            addSource(broadcastRepository.liveBroadcastLiveData) { broadcastEntity ->
                broadcast = broadcastEntity
                val showEntity = show ?: return@addSource
                postValue(Pair(showEntity, broadcastEntity))
            }
        }

    init {
        liveStreamLiveData.observeForever { (show, broadcast) ->
            Timber.d("updating metadata for ${show.name} ${broadcast?.date}")
            playbackPreparer.changeLiveMetadata(show, broadcast, isLiveNow.value ?: false)
        }
    }

    /** A [MediatorLiveData] which merges the currently playing show and currently playing broadcast. */
    val nowPlayingStreamLiveData = MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>()
        .apply {
            var broadcast: BroadcastEntity? = null
            var show: ShowEntity? = null

            addSource(broadcastRepository.nowPlayingShowLiveData) { showEntity ->
                show = showEntity
                postValue(Pair(showEntity, broadcast))
            }

            addSource(broadcastRepository.nowPlayingBroadcastLiveData) { broadcastEntity ->
                broadcast = broadcastEntity
                val showEntity = show ?: return@addSource
                postValue(Pair(showEntity, broadcastEntity))
            }
        }

    init {
        liveStreamLiveData.observeForever { (show, broadcast) ->
            Timber.d("updating metadata for ${show.name} ${broadcast?.date}")
            playbackPreparer.changeLiveMetadata(show, broadcast, isLiveNow.value ?: false)
        }
    }

    /** Runs a schedule scrape if it hasn't been fetched recently. */
    fun scrapeSchedule() = launch {
        val now = TimeHelper.getNow().toEpochSecond()
        val lastScrape = kdvsPreferences.lastScheduleScrape ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeSchedule()?.join()
        } else {
            Timber.d("Schedule has already been scraped recently; skipping")
        }
    }

    /**
     * Runs a schedule scrape without checking when it was last performed.
     * The only acceptable public usage of this method is when user explicitly refreshes.
     */
    private fun forceScrapeSchedule(): Job? = scraperManager.scrape(URLs.SCHEDULE)

    fun getCurrentQuarterYear(): LiveData<QuarterYear> = showDao.currentQuarterYear()

    fun getShows(): LiveData<List<ShowEntity>> =
        showDao.allShows()
            .debounce (100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    /** Fetches a [LiveData] that will wrap the show matching the provided ID. */
    fun showById(showId: Int): LiveData<ShowEntity> =
        showDao.showById(showId)

    fun showsForQuarterYear(quarterYear: QuarterYear): Flowable<List<ShowEntity>> {
        val (quarter, year) = quarterYear
        return showDao.allShowsByQuarterYear(quarter, year)
            .observeOn(Schedulers.io())
    }

    fun getShowsByQuarterYear(quarterYear: QuarterYear): List<ShowEntity> {
        val (quarter, year) = quarterYear
        return showDao.getShowsByQuarterYear(quarter, year)
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

    suspend fun allShowsAtTimeOrderedRelativeToCurrentWeek(timeStart: OffsetDateTime): List<ShowEntity?> {
        val liveShowUpdater = LiveShowUpdater(this, broadcastRepository, showDao)
        return liveShowUpdater.orderShowsAtTimeRelativeToCurrentWeekAsync(timeStart)
    }
}