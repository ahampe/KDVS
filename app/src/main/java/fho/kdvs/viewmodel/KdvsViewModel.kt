package fho.kdvs.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import fho.kdvs.playback.FocusManager
import fho.kdvs.playback.PlaybackFocusListener
import fho.kdvs.playback.RadioMediaPlayer
import fho.kdvs.repository.ShowRepository
import javax.inject.Inject

class KdvsViewModel @Inject constructor(
    showRepo: ShowRepository,
    application: Application
) : AndroidViewModel(application) {

    private val player = RadioMediaPlayer(streamUrl)

    val exoPlayer: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(application)

    init {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(
            application,
            Util.getUserAgent(application, "KDVS")
        )

        // This is the MediaSource representing the media to be played.
        val audioSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(wfmuStreamUrl))

        exoPlayer.prepare(audioSource)

        showRepo.printShows()
    }

    private val focusListener = object : PlaybackFocusListener {
        override fun onGainedAudioFocus() = player.start()
        override fun onLostAudioFocus() = player.pause()
        override fun onLostAudioFocusTransient() = player.pause()
        override fun onLostAudioFocusCanDuck() = player.duck()
    }
    private val focusManager = FocusManager(application.applicationContext, focusListener)

    val preparedState: LiveData<Boolean> = player.readyStateLiveData
    val playingState: LiveData<Boolean> = player.playbackStateLiveData

    fun changeToWmnf() {
        player.setNewUrl(wmnfStreamUrl)
    }

    fun changeToWfmu() {
        player.setNewUrl(wfmuStreamUrl)
    }

    fun changeToWvfs() {
        player.setNewUrl(wvfsStreamUrl)
    }

    fun togglePlay() {
        if (!player.isPlaying) {
            if (focusManager.isFocusGranted) {
                player.start()
            }
        } else {
            focusManager.abandonFocus()
            player.pause()
        }
    }

    override fun onCleared() {
        focusManager.abandonFocus()
        player.release()
        super.onCleared()
    }

    companion object {
        private const val streamUrl = "https://stream.wmnf.org:4443/wmnf_high_quality"
        private const val wfmuStreamUrl = "http://stream0.wfmu.org/freeform-128k"
        private const val wmnfStreamUrl = "https://stream.wmnf.org:4443/wmnf_high_quality"
        private const val wvfsStreamUrl = "http://voice.wvfs.fsu.edu:8000/stream"
    }
}