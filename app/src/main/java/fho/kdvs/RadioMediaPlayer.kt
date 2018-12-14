package fho.kdvs

import android.media.AudioAttributes
import android.media.MediaPlayer
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
        }
    }

    override fun start() {
        super.start()
        playbackStateLiveData.postValue(true)
    }

    override fun stop() {
        super.stop()
        playbackStateLiveData.postValue(false)
    }

    override fun pause() {
        super.pause()
        playbackStateLiveData.postValue(false)
    }

    companion object {
        private val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }
}