package fho.kdvs

import android.app.Application
import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import timber.log.Timber

class KdvsViewModel(application: Application) : AndroidViewModel(application) {
    private val audioMgr: AudioManager =
        application.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val listener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_GAIN -> Timber.d("AF gain")
            AudioManager.AUDIOFOCUS_LOSS -> Timber.d("AF loss")
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> Timber.d("AF loss transient")
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Timber.d("AF loss transient can duck")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val focusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(RadioMediaPlayer.audioAttrs)
            .setOnAudioFocusChangeListener(listener) // optional handler
            .build()

    private val player = RadioMediaPlayer(streamUrl)

    val preparedState: LiveData<Boolean> = player.readyStateLiveData
    val playingState: LiveData<Boolean> = player.playbackStateLiveData

    fun togglePlay() {
        if (!player.isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioMgr.requestAudioFocus(focusRequest)
            } else {
                audioMgr.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }
            player.start()
        } else {
            player.pause()
        }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }

    companion object {
        private const val streamUrl = "http://archives.kdvs.org:8000/kdvs128mp3"
    }
}