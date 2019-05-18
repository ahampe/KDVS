package fho.kdvs.global

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.id
import fho.kdvs.global.extensions.isPlayEnabled
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.util.URLs
import fho.kdvs.global.util.URLs.DISCOGS_QUERYSTRING
import fho.kdvs.global.util.URLs.DISCOGS_SEARCH_URL
import fho.kdvs.global.util.URLs.YOUTUBE_QUERYSTRING
import fho.kdvs.global.util.URLs.YOUTUBE_SEARCH_URL
import fho.kdvs.services.LiveShowUpdater
import fho.kdvs.services.MediaSessionConnection
import fho.kdvs.show.ShowRepository
import fho.kdvs.track.TrackRepository
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

    var nowPlayingPreviewPlayButton: ImageView?= null
    var nowPlayingPlayButton: ImageView?= null

    val liveStreamLiveData: MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>
        get() = showRepository.liveStreamLiveData

    val nowPlayingStreamLiveData: MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>
        get() = showRepository.nowPlayingStreamLiveData

    val nowPlayingShow: LiveData<ShowEntity>
        get() = broadcastRepository.nowPlayingShowLiveData

    val liveShow: LiveData<ShowEntity>
        get() = showRepository.liveShowLiveData

    val nextLiveShow: LiveData<ShowEntity>
        get() = showRepository.nextShowLiveData

    val nowPlayingBroadcast: LiveData<BroadcastEntity>
        get() = broadcastRepository.nowPlayingBroadcastLiveData

    val liveBroadcast: LiveData<BroadcastEntity>
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

    fun playOrPausePlayback() {
        if (mediaSessionConnection.playbackState.value?.isPrepared == false)
            changeToKdvsOgg()

        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) {
                transportControls.pause()
                onPause()
            }
            else {
                transportControls.play()
                onPlay()
            }
        }
    }

    fun playLiveShowFromHome() {
        broadcastRepository.nowPlayingShowLiveData.postValue(showRepository.liveShowLiveData.value)
        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcastRepository.liveBroadcastLiveData.value)
        changeToKdvsOgg()
    }

    fun stopPlayback() {
        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) transportControls.stop() }
    }

    private fun prepareLivePlayback(streamUrl: String) {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls ?: return

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false

        if (isPrepared && streamUrl == nowPlaying?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> { transportControls.pause(); onPause() }
                    playbackState.isPlayEnabled -> { transportControls.play(); onPlay() }
                    else -> {
                        Timber.w("Playable item clicked but neither play nor pause are enabled! (mediaId=$streamUrl)")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(streamUrl, null)
            onPlay()
        }
    }

    private fun onPause() {
        nowPlayingPreviewPlayButton?.setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
        nowPlayingPlayButton?.setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
    }

    private fun onPlay() {
        nowPlayingPreviewPlayButton?.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp)
        nowPlayingPlayButton?.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp)
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

    fun onClickDiscogs(view: View, track: TrackEntity?) {
        if (track == null) return
        openBrowser(view, "$DISCOGS_SEARCH_URL${track.artist} ${track.song}$DISCOGS_QUERYSTRING")
    }

    fun onClickYoutube(view: View, track: TrackEntity?) {
        if (track == null) return
        openBrowser(view, "$YOUTUBE_SEARCH_URL${track.artist} ${track.song}$YOUTUBE_QUERYSTRING")
    }

    fun onClickSpotifyNoApp(view: View, spotifyUri: String?) {
        val url = makeSpotifyUrl(spotifyUri ?: "")
        if (url.isNotEmpty())
            openBrowser(view, url)
    }

    fun openSpotifyApp(view: View, spotifyUri: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply{
            data = Uri.parse(spotifyUri)
            putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + view.context.packageName))
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        } else {
            onClickSpotifyNoApp(view, spotifyUri)
        }
    }

    private fun makeSpotifyUrl(spotifyUri: String): String {
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