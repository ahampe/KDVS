package fho.kdvs.playback

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class RadioMediaPlayer(sourceUrl: String) : MediaPlayer() {
    // this LiveData will keep track of the player's prepared state
    val readyStateLiveData = MutableLiveData<Boolean>()
    // this LiveData will keep track of whether the player is playing (true) or paused / stopped (false)
    val playbackStateLiveData = MutableLiveData<Boolean>()

    var source: String? = null

    init {
        prepareMediaPlayer(sourceUrl)
    }

    fun setNewUrl(newUrl: String) {
        prepareMediaPlayer(newUrl)
    }

    override fun start() {
        setVolume(1f, 1f)
        super.start()
        playbackStateLiveData.postValue(true)
    }

    fun duck() {
        setVolume(0.2f, 0.2f)
    }

    override fun stop() {
        super.stop()
        playbackStateLiveData.postValue(false)
    }

    override fun pause() {
        super.pause()
        playbackStateLiveData.postValue(false)
    }

    private fun prepareMediaPlayer(url: String) {
        // no need to reload if sources match
        if (url == source) return
        source = url

        if (isPlaying) {
            stop()
        }
        reset()

        readyStateLiveData.value = false

        setDataSource(url)
        setAudioAttributes(AudioHelper.attrs)

        // Prepare the audio stream in the background, and enable the play button only when ready
        prepareAsync()

        setOnPreparedListener {
            readyStateLiveData.postValue(true)
        }

        setOnErrorListener { _, what, _ ->
            when (what) {
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> Timber.d("Unknown media error")
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Timber.d("Server died")
            }
            return@setOnErrorListener false
        }
    }
}