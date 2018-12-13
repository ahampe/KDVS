package fho.kdvs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import java.lang.Exception

class KdvsViewModel(application: Application) : AndroidViewModel(application) {
    val player = RadioMediaPlayer(streamUrl)
    val preparedState: LiveData<Boolean> = player.readyStateLiveData
    val playingState: LiveData<Boolean> = player.playbackStateLiveData

    fun togglePlay() {
        try {
            if (!player.isPlaying) {
                player.start()
            } else {
                player.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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