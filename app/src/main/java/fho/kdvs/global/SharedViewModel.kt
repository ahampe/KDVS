package fho.kdvs.global

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.id
import fho.kdvs.global.extensions.isPlayEnabled
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.Spotify
import fho.kdvs.services.LiveShowUpdater
import fho.kdvs.services.MediaSessionConnection
import fho.kdvs.show.ShowRepository
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/** An [AndroidViewModel] scoped to the main activity.
 * Use this for data that will be consumed in many places. */
class SharedViewModel @Inject constructor(
    application: Application,
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val trackRepository: TrackRepository,
    private val liveShowUpdater: LiveShowUpdater,
    private val mediaSessionConnection: MediaSessionConnection
) : BaseViewModel(application) {

    val nowPlaying: LiveData<ShowEntity>
        get() = showRepository.playingShowLiveData

    val currentShow: LiveData<ShowEntity>
        get() = showRepository.playingShowLiveData

    val nextShow: LiveData<ShowEntity>
        get() = showRepository.nextShowLiveData

    val currentBroadcast: LiveData<BroadcastEntity>
        get() = broadcastRepository.liveBroadcastLiveData

    val isLiveNow: LiveData<Boolean> = showRepository.isLiveNow

    fun updateLiveShows() = liveShowUpdater.beginUpdating()

    fun fetchShows() = showRepository.scrapeSchedule()

    // region playback

    fun changeToKdvsMp3() {
        prepareLivePlayback(URLs.LIVE_MP3)
    }

    fun changeToKdvsAac() {
        prepareLivePlayback(URLs.LIVE_AAC)
    }

    fun changeToKdvsOgg() {
        prepareLivePlayback(URLs.LIVE_OGG)
    }

    private fun prepareLivePlayback(streamUrl: String) {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls ?: return

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false

        if (isPrepared && streamUrl == nowPlaying?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Timber.w("Playable item clicked but neither play nor pause are enabled! (mediaId=$streamUrl)")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(streamUrl, null)
        }
    }

    // endregion

    // region Launch Activity

    fun composeEmail(view: View, address: String?) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        }
    }

    fun openBrowser(view: View, url: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        }
    }

    fun openSpotifyApp(view: View, spotifyUri: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply{
            data = Uri.parse(spotifyUri)
            putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + view.context.packageName))
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        }
    }

    // endregion

    // region Spotify

    // TODO: refactor this code to Spotify.kt

    fun onTrackSpotifyClick(view: View, track: TrackEntity?) {
        Timber.d("Spotify icon clicked for ${track?.song}")

        if (track == null) return

        launch {
            var spotifyUri = track.spotifyUri
            if (spotifyUri.isNullOrEmpty()) {
//
//                launch {
//                    val liveUri = trackRepository.spotifyUriById(track.trackId)
//                    liveUri.observe(this, Observer {
//
//                    })
//                }

                val response = Spotify.searchForTrack(track)
                spotifyUri = Spotify.parseSpotifyTrackUri(response)

                launch { trackRepository.updateTrackSpotifyUri(track.trackId, spotifyUri) }

            }

            if (spotifyUri.isNotEmpty())
                openSpotify(view, spotifyUri)
        }
    }

    fun openSpotify(view: View, spotifyUri: String) {
        if (isSpotifyInstalledOnDevice(view)) {
            openSpotifyApp(view, spotifyUri)
        }
        else {
            val url = makeSpotifyUrl(spotifyUri)
            if (url.isNotEmpty())
                openBrowser(view, url)
        }
    }

    private fun isSpotifyInstalledOnDevice(view: View): Boolean {
        var isSpotifyInstalled = false

        try {
            view.context.packageManager.getPackageInfo("com.spotify.music", 0)
            isSpotifyInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {}

        return isSpotifyInstalled
    }

    fun makeSpotifyUrl(spotifyUri: String): String {
        var url = ""

        val re = "spotify:(\\w+):(.+)".toRegex().find(spotifyUri)
        val type = re?.groupValues?.getOrNull(1)
        val id = re?.groupValues?.getOrNull(2)

        if (!type.isNullOrEmpty() && !id.isNullOrEmpty())
            url = "https://open.spotify.com/$type/$id"

        return url
    }

    // endregion
}