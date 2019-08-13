package fho.kdvs.global

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs.DISCOGS_QUERYSTRING
import fho.kdvs.global.util.URLs.DISCOGS_SEARCH_URL
import fho.kdvs.global.util.URLs.YOUTUBE_QUERYSTRING
import fho.kdvs.global.util.URLs.YOUTUBE_SEARCH_URL
import fho.kdvs.schedule.QuarterRepository
import fho.kdvs.schedule.QuarterYear
import fho.kdvs.services.*
import fho.kdvs.show.ShowRepository
import fho.kdvs.subscription.SubscriptionRepository
import kotlinx.coroutines.launch
import org.jetbrains.anko.runOnUiThread
import javax.inject.Inject


/** An [AndroidViewModel] scoped to the main activity.
 * Use this for data that will be consumed in many places. */
class SharedViewModel @Inject constructor(
    application: Application,
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val quarterRepository: QuarterRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val favoriteDao: FavoriteDao,
    private val subscriptionDao: SubscriptionDao,
    private val liveShowUpdater: LiveShowUpdater,
    private val mediaSessionConnection: MediaSessionConnection
) : BaseViewModel(application) {

    val liveStreamLiveData: MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>
        get() = showRepository.liveStreamLiveData

    val nowPlayingStreamLiveData: MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>
        get() = showRepository.nowPlayingStreamLiveData

    val nowPlayingShow: LiveData<ShowEntity>
        get() = broadcastRepository.nowPlayingShowLiveData

    val liveShow: LiveData<ShowEntity>
        get() = showRepository.liveShowLiveData

    val nextLiveShow: LiveData<ShowEntity>
        get() = showRepository.nextShowLiveData

    val nowPlayingBroadcast: LiveData<BroadcastEntity>
        get() = broadcastRepository.nowPlayingBroadcastLiveData

    val liveBroadcast: LiveData<BroadcastEntity>
        get() = broadcastRepository.liveBroadcastLiveData

    val isPlayingAudioNow = mediaSessionConnection.playbackState

    val isLiveNow: LiveData<Boolean?> = showRepository.isLiveNow

    /** Use across various lifecycles of PlayerFragment to maintain list of scraped tracks for live broadcast. */
    var scrapedTracksForBroadcast= mutableListOf<TrackEntity>()

    fun updateLiveShows() = liveShowUpdater.beginUpdating()

    fun fetchShows() = showRepository.scrapeSchedule()

    // region quarter

    /** All quarter-years in the database. */
    val allQuarterYearsLiveData = quarterRepository.allQuarterYearsLiveData

    /** The current real quarter-year */
    private val currentQuarterYearLiveData = showRepository.getCurrentQuarterYear()

    /** The currently selected quarter-year. */
    val selectedQuarterYearLiveData = quarterRepository.selectedQuarterYearLiveData

    /** Sets the given [QuarterYear]. Change will be reflected in [selectedQuarterYearLiveData]. */
    fun selectQuarterYear(quarterYear: QuarterYear) =
        quarterRepository.selectQuarterYear(quarterYear)

    /**
     * Gets the selected [QuarterYear] if there is one, else returns the most recent [QuarterYear].
     * If both are null, returns null.
     *
     * Note: It's important that [allQuarterYearsLiveData] has observers, otherwise it will not be updated.
     * This doesn't hold for [selectedQuarterYearLiveData] as it's not sourced from RxJava.
     */
    fun loadQuarterYear(): QuarterYear? =
        selectedQuarterYearLiveData.value ?: allQuarterYearsLiveData.value?.firstOrNull()

    /**
     * Code to execute when a new current QuarterYear is observed.
     */
    fun onNewQuarter(context: Context?) {
        processSubscriptionsOnQuarterChange()
        makeNewQuarterToast(context)
    }

    private fun makeNewQuarterToast(context: Context?) =
        Toast.makeText(context, "The new quarter has begun!", Toast.LENGTH_SHORT)
            .show()

    private fun getCurrentQuarterShows(): List<ShowEntity>? {
        currentQuarterYearLiveData.value?.let {
            return showRepository.getShowsByQuarterYear(it)
        }

        return null
    }

    // endregion

    // region playback

    fun navigateToPlayer(navController: NavController) {
        navController.navigate(R.id.playerFragment)
    }

    fun playOrPausePlayback() {
        if (mediaSessionConnection.playbackState.value?.isPrepared == false)
            prepareLivePlayback()

        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) {
                transportControls.pause()
            }
            else {
                transportControls.play()
            }
        }
    }

    fun playLiveShowFromHome() {
        broadcastRepository.playingLiveBroadcast = true
        broadcastRepository.nowPlayingShowLiveData.postValue(showRepository.liveShowLiveData.value)
        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcastRepository.liveBroadcastLiveData.value)
        prepareLivePlayback()
    }

    fun playPastBroadcast(broadcast: BroadcastEntity, show: ShowEntity) {
        mediaSessionConnection.transportControls?.playFromMediaId(
            broadcast.broadcastId.toString(),
            Bundle().apply {
                putInt("SHOW_ID", show.id)
                putString("TYPE", PlaybackType.ARCHIVE.type)
            }
        )

        mediaSessionConnection.isLiveNow.postValue(false)
        broadcastRepository.playingLiveBroadcast = false

        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcast)
        broadcastRepository.nowPlayingShowLiveData.postValue(show)
    }

    fun stopPlayback() {
        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) transportControls.stop() }
    }

    fun prepareLivePlayback() {
        val customAction = CustomAction(getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value,
            mediaSessionConnection)

        customAction.live()
    }

    fun jumpBack30Seconds() {
        val customAction = CustomAction(getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value,
            mediaSessionConnection)

        customAction.replay()
    }

    fun jumpForward30Seconds() {
        val customAction = CustomAction(getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value,
            mediaSessionConnection)

        customAction.forward()
    }

    fun isShowBroadcastLiveNow(show: ShowEntity, broadcast: BroadcastEntity?): Boolean{
        return isLiveNow.value == null ||
            broadcast == null ||
                TimeHelper.isShowBroadcastLive(show, broadcast)
    }

    // endregion

    // region Launch Activity

    fun composeEmail(view: View, address: String?) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        }
    }

    fun openBrowser(view: View, url: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        }
    }

    fun onClickDiscogs(view: View, track: TrackEntity?) {
        if (track == null) return
        openBrowser(view, "$DISCOGS_SEARCH_URL${track.artist} ${track.song}$DISCOGS_QUERYSTRING")
    }

    fun onClickYoutube(view: View, track: TrackEntity?) {
        if (track == null) return
        openBrowser(view, "$YOUTUBE_SEARCH_URL${track.artist} ${track.song}$YOUTUBE_QUERYSTRING")
    }

    fun onClickSpotifyNoApp(view: View, spotifyUri: String?) {
        val url = makeSpotifyUrl(spotifyUri ?: "")
        if (url.isNotEmpty())
            openBrowser(view, url)
    }

    fun openSpotifyApp(view: View, spotifyUri: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply{
            data = Uri.parse(spotifyUri)
            putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + view.context.packageName))
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        } else {
            onClickSpotifyNoApp(view, spotifyUri)
        }
    }

    private fun makeSpotifyUrl(spotifyUri: String): String {
        var url = ""

        val re = "spotify:(\\w+):(.+)".toRegex().find(spotifyUri)
        val type = re?.groupValues?.getOrNull(1)
        val id = re?.groupValues?.getOrNull(2)

        if (!type.isNullOrEmpty() && !id.isNullOrEmpty())
            url = "https://open.spotify.com/$type/$id"

        return url
    }

    // endregion

    // region UI

    fun onClickFavorite(view: View, trackId: Int) {
        val imageView = view as? ImageView

        if (imageView?.tag == 0) {
            imageView.setImageResource(R.drawable.ic_favorite_white_24dp)
            imageView.tag = 1
            launch { favoriteDao.insert(FavoriteEntity(0, trackId)) }
        } else if (imageView?.tag == 1) {
            imageView.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            imageView.tag = 0
            launch { favoriteDao.deleteByTrackId(trackId) }
        }
    }

    fun onClickStar(imageView: ImageView, show: ShowEntity, context: Context?) {
        if (imageView.tag == 0) {
            subscribeToShowAndMakeToast(show, context)

            imageView.setImageResource(R.drawable.ic_star_border_white_24dp)
            imageView.tag = 1
        } else if (imageView.tag == 1) {
            cancelSubscription(show)
            Toast.makeText(context, "Unsubscribed from ${show.name}", Toast.LENGTH_SHORT)
                .show()

            imageView.setImageResource(R.drawable.ic_star_white_24dp)
            imageView.tag = 0
        }
    }
    // endregion

    // region subscription

    /**
     * After a quarter change, subscribed recurring shows will have new database objects, and possibly new timeslots,
     * so we must cancel existing ones and insert new ones based on matching show names in the current quarter.
     * Nonrecurring shows will simply have their subscriptions cancelled.
     */
    private fun processSubscriptionsOnQuarterChange() {
        launch {
            val subscribedShows = getSubscribedShows()
            val recurringSubscribedShows = getRecurringSubscribedShows(subscribedShows)
            val nonRecurringSubscribedShows = subscribedShows
                .filterNot { s -> recurringSubscribedShows?.contains(s) == true }

            recurringSubscribedShows?.forEach {
                updateRecurringSubscription(it)
            }

            nonRecurringSubscribedShows.forEach {
                cancelSubscription(it)
            }
        }
    }

    private fun updateRecurringSubscription(show: ShowEntity) {
        cancelSubscription(show)

        val currentShows = getCurrentQuarterShows()
        val matchingShow = currentShows
            ?.firstOrNull { s -> s.name == show.name }

        matchingShow?.let {
            subscribeToShowWithoutToast(it)
        }
    }

    private fun subscribeToShowWithoutToast(show: ShowEntity) {
        launch {
            val success = subscribeToShow(show)

            if (success) {
                launch { subscriptionDao.insert(SubscriptionEntity(0, show.id)) }
            }
        }
    }

    private fun subscribeToShowAndMakeToast(show: ShowEntity, context: Context?) {
        launch {
            val success = subscribeToShow(show)

            if (success) {
                launch { subscriptionDao.insert(SubscriptionEntity(0, show.id)) }
                context?.runOnUiThread {
                    Toast.makeText(context, "Subscribed to ${show.name}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private suspend fun subscribeToShow(show: ShowEntity): Boolean {
        val alarmMgr = KdvsAlarmManager(getApplication(), showRepository)
        return alarmMgr.registerShowAlarmAsync(show).await()
    }

    private fun cancelSubscription(show: ShowEntity) {
        launch { subscriptionDao.deleteByShowId(show.id) }
        launch {
            val alarmMgr = KdvsAlarmManager(getApplication(), showRepository)
            alarmMgr.cancelShowAlarm(show)
        }
    }

    private fun getSubscribedShows(): List<ShowEntity> {
        return subscriptionRepository.subscribedShows()
    }

    private fun getRecurringSubscribedShows(subscribedShows: List<ShowEntity>): List<ShowEntity>? {
        val currentShows = getCurrentQuarterShows()

        currentShows?.let {
            return subscribedShows
                .filter { show ->
                    currentShows
                    .map { s -> s.name }
                    .contains(show.name)
                }
        }

        return null
    }

    // endregion
}
