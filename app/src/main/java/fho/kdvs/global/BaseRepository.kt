package fho.kdvs.global

import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import fho.kdvs.global.extensions.id
import fho.kdvs.global.extensions.isPlaying
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

    val isLiveNow: LiveData<Boolean?> get() = mediaSessionConnection.isLiveNow
}