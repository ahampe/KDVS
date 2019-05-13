package fho.kdvs.broadcast

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.BroadcastDao
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.WebScraperManager
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class BroadcastRepository @Inject constructor(
    private val trackRepository: TrackRepository,
    private val broadcastDao: BroadcastDao,
    private val scraperManager: WebScraperManager,
    private val kdvsPreferences: KdvsPreferences
) : BaseRepository() {

    /** The broadcast currently being played by the user. This may or may not be [liveBroadcastLiveData]. */
    val playingBroadcastLiveData: LiveData<BroadcastEntity> get() = _playingBroadcastLiveData

    /**
     * The currently playing broadcast.
     * Whenever the value is set, a request to scrape its details is sent to [BroadcastRepository].
     */
    private val _playingBroadcastLiveData = object : MutableLiveData<BroadcastEntity>() {
        override fun setValue(value: BroadcastEntity?) {
            value?.let { trackRepository.scrapePlaylist(it.broadcastId.toString()) }
            super.setValue(value)
        }
    }

    /** [MutableLiveData] listening for the currently streaming broadcast. */
    val liveBroadcastLiveData = MutableLiveData<BroadcastEntity>()
    // this private LiveData will listen for the most recent broadcast from the live show and notify BroadcastRepository
    private var _liveBroadcastLiveData: LiveData<BroadcastEntity>? = null

    /**
     * This [Observer] will connect the private LiveData to the public LiveData.
     * This is necessary because [_liveBroadcastLiveData] changes references with the show ID,
     * whereas we want to have a single reference for the public API.
     */
    private val liveBroadcastObserver = Observer<BroadcastEntity> { br ->
        val broadcast = br ?: return@Observer

        // The database just listens for the most recent broadcast, which may be last week's.
        // We can prevent outdated broadcasts from reaching the UI with this check,
        // which is generally lenient since broadcasts happen at most weekly on KDVS.
        Timber.d("live show's most recent broadcast: $broadcast")
        val broadcastDate = broadcast.date ?: return@Observer
        if (abs(ChronoUnit.DAYS.between(LocalDate.now(), broadcastDate)) < 3L) {
            Timber.d("broadcast is this week. posting...")
            liveBroadcastLiveData.postValue(broadcast)
        }
    }

    init {

    }


    /** Changes playback to a selected past broadcast. */
    fun playPastBroadcast(broadcast: BroadcastEntity, showId: Int) {
        mediaSessionConnection.transportControls?.playFromMediaId(
            broadcast.broadcastId.toString(),
            Bundle().apply { putInt("SHOW_ID", showId) }
        )

        _playingBroadcastLiveData.postValue(broadcast)
    }

    /** Runs a show scrape if it hasn't been fetched recently. */
    fun scrapeShow(showId: String): Job = launch {
        val now = OffsetDateTime.now().toEpochSecond()
        val lastScrape = kdvsPreferences.getLastShowScrape(showId) ?: 0L
        val scrapeFreq = kdvsPreferences.scrapeFrequency ?: WebScraperManager.DEFAULT_SCRAPE_FREQ

        if (now - lastScrape > scrapeFreq) {
            forceScrapeShow(showId)?.join()
        } else {
            Timber.d("Show $showId has already been scraped recently; skipping")
        }
    }

    /**
     * Runs a show scrape without checking when it was last performed.
     * The only acceptable public usage of this method is when user explicitly refreshes.
     */
    fun forceScrapeShow(showId: String): Job? = scraperManager.scrape(URLs.showDetails(showId))

    fun broadcastById(broadcastId: Int): LiveData<BroadcastEntity> =
        broadcastDao.broadcastById(broadcastId)

    fun broadcastsForShow(showId: Int): LiveData<List<BroadcastEntity>> =
        broadcastDao.allBroadcastsForShow(showId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    fun getLatestBroadcastForShow(showId: Int): BroadcastEntity? =
        broadcastDao.getLatestBroadcastForShow(showId)

    fun showByBroadcastId(broadcastId: Int): LiveData<ShowEntity> =
        broadcastDao.showByBroadcastId(broadcastId)

    /** Called when the live show changes so that the playing broadcast can be updated */
    internal fun updateLiveBroadcast(showId: Int) {
        launch(mainContext) { _liveBroadcastLiveData?.removeObserver(liveBroadcastObserver) }

        _liveBroadcastLiveData = latestBroadcastForShow(showId)

        launch(mainContext) { _liveBroadcastLiveData?.observeForever(liveBroadcastObserver) }
    }

    private fun latestBroadcastForShow(showId: Int): LiveData<BroadcastEntity> =
        broadcastDao.latestBroadcastForShow(showId)
}