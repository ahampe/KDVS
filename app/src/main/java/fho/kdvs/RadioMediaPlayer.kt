package fho.kdvs

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData

class RadioMediaPlayer(sourceUrl: String) : MediaPlayer() {
    // this LiveData will keep track of the player's prepared state
    val readyStateLiveData = MutableLiveData<Boolean>()

    init {
        // disable play button while preparing
        readyStateLiveData.value = false

        setDataSource(sourceUrl)
        setAudioAttributes(audioAttrs)

        // Prepare the audio stream in the background, and enable the play button only when ready
        prepareAsync()
        setOnPreparedListener {
            readyStateLiveData.postValue(true)
        }
    }

    companion object {
        private val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }
}