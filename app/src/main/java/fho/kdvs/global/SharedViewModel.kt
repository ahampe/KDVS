package fho.kdvs.global

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.id
import fho.kdvs.global.extensions.isPlayEnabled
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.util.URLs
import fho.kdvs.services.LiveShowUpdater
import fho.kdvs.services.MediaSessionConnection
import fho.kdvs.show.ShowRepository
import timber.log.Timber
import javax.inject.Inject

/** An [AndroidViewModel] scoped to the main activity.
 * Use this for data that will be consumed in many places. */
class SharedViewModel @Inject constructor(
    application: Application,
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val liveShowUpdater: LiveShowUpdater,
    private val mediaSessionConnection: MediaSessionConnection
) : BaseViewModel(application) {

    val nowPlaying: LiveData<ShowEntity>
        get() = showRepository.playingShowLiveData

    val currentShow: LiveData<ShowEntity>
        get() = showRepository.playingShowLiveData

    val nextShow: LiveData<ShowEntity>
        get() = showRepository.nextShowLiveData

    val currentBroadcast: LiveData<BroadcastEntity>
        get() = broadcastRepository.liveBroadcastLiveData

    val isLiveNow: LiveData<Boolean> = showRepository.isLiveNow

    fun updateLiveShows() = liveShowUpdater.beginUpdating()

    fun fetchShows() = showRepository.scrapeSchedule()

    // region playback

    fun changeToKdvsMp3() {
        prepareLivePlayback(URLs.LIVE_MP3)
    }

    fun changeToKdvsAac() {
        prepareLivePlayback(URLs.LIVE_AAC)
    }

    fun changeToKdvsOgg() {
        prepareLivePlayback(URLs.LIVE_OGG)
    }

    private fun prepareLivePlayback(streamUrl: String) {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls ?: return

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false

        if (isPrepared && streamUrl == nowPlaying?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Timber.w("Playable item clicked but neither play nor pause are enabled! (mediaId=$streamUrl)")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(streamUrl, null)
        }
    }

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

    fun openSpotify(track: TrackEntity?) {

    }

    // endregion
}