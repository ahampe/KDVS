package fho.kdvs.playback

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

/** A [MediaPlayer] that holds [MutableLiveData] objects describing its state.
 * Only one player should be necessary during the app's lifecycle.
 * If the stream source changes, use [setNewUrl]. */
class RadioMediaPlayer(sourceUrl: String) : MediaPlayer() {
    // this LiveData will keep track of the player's prepared state
    val readyStateLiveData = MutableLiveData<Boolean>()
        .apply { postValue(false) }

    // this LiveData will keep track of whether the player is playing (true) or paused / stopped (false)
    val playbackStateLiveData = MutableLiveData<Boolean>()
        .apply { postValue(false) }

    // reference current source URL so that we don't reload when new source == old source
    private var source: String? = null

    init {
        setAudioAttributes(AudioHelper.attrs)

        setOnPreparedListener {
            readyStateLiveData.postValue(true)
        }

        setOnErrorListener { _, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> Timber.d("Unknown media error $extra")
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Timber.d("Server died")
            }
            return@setOnErrorListener false
        }

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

    override fun stop() {
        super.stop()
        playbackStateLiveData.postValue(false)
    }

    override fun pause() {
        super.pause()
        playbackStateLiveData.postValue(false)
    }

    // Only need to call this on pre-Oreo OS versions, where it's not handled automatically.
    fun duck() {
        setVolume(0.2f, 0.2f)
    }

    private fun prepareMediaPlayer(url: String) {
        // no need to reload if sources match
        if (url == source) return
        source = url

        // disable play/pause until prepared
        readyStateLiveData.value = false

        // re-initialize player with new url
        if (isPlaying) {
            stop()
        }
        reset()
        setDataSource(url)
        prepareAsync()
    }
}