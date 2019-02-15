package fho.kdvs.global

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fho.kdvs.global.extensions.id
import fho.kdvs.global.extensions.isPlayEnabled
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.util.URLs
import fho.kdvs.services.MediaSessionConnection
import timber.log.Timber
import javax.inject.Inject

/** An [AndroidViewModel] scoped to the main activity.
 * Use this for data that will be consumed in many places. */
class SharedViewModel @Inject constructor(
    application: Application,
    private val mediaSessionConnection: MediaSessionConnection
) : AndroidViewModel(application) {

    // region playback

    fun changeToWmnf() {
        prepareLivePlayback(URLs.WMNF)
    }

    fun changeToWfmu() {
        prepareLivePlayback(URLs.WFMU)
    }

    fun changeToKdvsMp3() {
        prepareLivePlayback(URLs.LIVE_MP3)
    }

    fun changeToKdvsAac() {
        prepareLivePlayback(URLs.LIVE_AAC)
    }

    fun changeToKdvsOgg() {
        prepareLivePlayback(URLs.LIVE_OGG)
    }

    private fun prepareLivePlayback(urlString: String) {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false

        if (isPrepared && urlString == nowPlaying?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Timber.w("Playable item clicked but neither play nor pause are enabled! (mediaId=$urlString)")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(urlString, null)
        }
    }

    // endregion
}