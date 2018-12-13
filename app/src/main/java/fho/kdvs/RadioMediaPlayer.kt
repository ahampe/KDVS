package fho.kdvs

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.MutableLiveData

class RadioMediaPlayer(sourceUrl: String) : MediaPlayer() {
    // this LiveData will keep track of the player's prepared state
    val readyStateLiveData = MutableLiveData<Boolean>()
    // this LiveData will keep track of whether the player is playing (true) or paused / stopped (false)
    val playbackStateLiveData = MutableLiveData<Boolean>()

    init {
        readyStateLiveData.value = false

        setDataSource(sourceUrl)
        setAudioAttributes(audioAttrs)

        // Prepare the audio stream in the background, and enable the play button only when ready
        prepareAsync()
        setOnPreparedListener {
            readyStateLiveData.postValue(true)
            Log.d("DAVISCA", "Updated RadioMediaPlayer LiveData")
        }
    }

    override fun start() {
        playbackStateLiveData.postValue(true)
        super.start()
    }

    override fun stop() {
        playbackStateLiveData.postValue(false)
        super.stop()
    }

    override fun pause() {
        playbackStateLiveData.postValue(false)
        super.pause()
    }

    companion object {
        private val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }
}