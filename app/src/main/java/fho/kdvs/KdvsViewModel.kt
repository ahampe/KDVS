package fho.kdvs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.playback.FocusManager
import fho.kdvs.playback.PlaybackFocusListener
import fho.kdvs.playback.RadioMediaPlayer

class KdvsViewModel(application: Application) : AndroidViewModel(application) {

    private val player = RadioMediaPlayer(streamUrl)

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
    }
}