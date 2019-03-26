package fho.kdvs.global

import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import fho.kdvs.global.extensions.id
import fho.kdvs.global.util.URLs
import fho.kdvs.services.EMPTY_PLAYBACK_STATE
import fho.kdvs.services.MediaSessionConnection
import fho.kdvs.services.NOTHING_PLAYING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * A base class for repositories.
 * Allows the repository to launch coroutines easily with [launch][kotlinx.coroutines.launch].
 * By default, coroutines will launch using [Dispatchers.IO]. Launch with [mainContext] if you need the main thread.
 */
abstract class BaseRepository : CoroutineScope {
    @Inject
    lateinit var mediaSessionConnection: MediaSessionConnection

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val mainContext: CoroutineContext
        get() = Dispatchers.Main + job

    /** Whether or not the user is playing KDVS live */
//    val isLiveNow: Boolean
//        get() {
//            val nowPlaying = mediaSessionConnection.nowPlaying.value ?: return false
//            val playingUri = nowPlaying.id
//            return URLs.liveStreamUrls.contains(playingUri)
//        }

    val isLiveNow: LiveData<Boolean> get() = mediaSessionConnection.isLiveNow

    /** Listener for both playback state (first in pair) and "live now" state (second in pair) */
    val statusLiveData by lazy {
        MediatorLiveData<Pair<PlaybackStateCompat, Boolean>>()
            .apply {
                var playbackState = EMPTY_PLAYBACK_STATE
                var isLiveNow = false

                addSource(mediaSessionConnection.playbackState) { state ->
                    playbackState = state
                    postValue(Pair(state, isLiveNow))
                }

                addSource(mediaSessionConnection.isLiveNow) { isLive ->
                    isLiveNow = isLive
                    postValue(Pair(playbackState, isLive))
                }
            }
    }
}