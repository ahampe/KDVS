package fho.kdvs.global

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import fho.kdvs.global.util.URLs
import timber.log.Timber
import javax.inject.Inject

/** An [AndroidViewModel] scoped to the main activity.
 * Use this for data that will be consumed in many places. */
class SharedViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // TODO
    val exoPlayer: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(application)

    init {
        prepareLivePlayback(URLs.LIVE_AAC)

        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException?) {
                // TODO
                Timber.d("Player error: $error")
            }
        })
    }

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

    override fun onCleared() {
        Timber.d("Clearing SharedViewModel...")
        exoPlayer.release()
        super.onCleared()
    }

    private fun prepareLivePlayback(urlString: String) {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(
            getApplication(),
            Util.getUserAgent(getApplication(), "KDVS")
        )

        // This is the MediaSource representing the media to be played.
        val audioSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(urlString))

        exoPlayer.prepare(audioSource)
    }

    // endregion
}