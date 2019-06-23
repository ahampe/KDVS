package fho.kdvs.global

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs.DISCOGS_QUERYSTRING
import fho.kdvs.global.util.URLs.DISCOGS_SEARCH_URL
import fho.kdvs.global.util.URLs.YOUTUBE_QUERYSTRING
import fho.kdvs.global.util.URLs.YOUTUBE_SEARCH_URL
import fho.kdvs.global.web.*
import fho.kdvs.services.CustomAction
import fho.kdvs.services.LiveShowUpdater
import fho.kdvs.services.MediaSessionConnection
import fho.kdvs.services.PlaybackType
import fho.kdvs.show.ShowRepository
import fho.kdvs.show.TopMusicRepository
import kotlinx.coroutines.launch
import javax.inject.Inject


/** An [AndroidViewModel] scoped to the main activity.
 * Use this for data that will be consumed in many places. */
@kotlinx.serialization.UnstableDefault
class SharedViewModel @Inject constructor(
    application: Application,
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteDao: FavoriteDao,
    private val subscriptionDao: SubscriptionDao,
    private val liveShowUpdater: LiveShowUpdater,
    private val mediaSessionConnection: MediaSessionConnection
) : BaseViewModel(application) {

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

    val isPlayingAudioNow = mediaSessionConnection.playbackState

    val isLiveNow: LiveData<Boolean?> = showRepository.isLiveNow

    /** Use across various lifecycles of PlayerFragment to maintain list of scraped tracks for live broadcast. */
    var scrapedTracksForBroadcast= mutableListOf<TrackEntity>()

    fun updateLiveShows() = liveShowUpdater.beginUpdating()

    fun fetchShows() = showRepository.scrapeSchedule()

    // region playback

    fun navigateToPlayer(navController: NavController) {
        navController.navigate(R.id.playerFragment)
    }

    fun playOrPausePlayback() {
        if (mediaSessionConnection.playbackState.value?.isPrepared == false)
            prepareLivePlayback()

        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) {
                transportControls.pause()
            }
            else {
                transportControls.play()
            }
        }
    }

    fun playLiveShowFromHome() {
        broadcastRepository.playingLiveBroadcast = true
        broadcastRepository.nowPlayingShowLiveData.postValue(showRepository.liveShowLiveData.value)
        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcastRepository.liveBroadcastLiveData.value)
        prepareLivePlayback()
    }

    fun playPastBroadcast(broadcast: BroadcastEntity, show: ShowEntity) {
        mediaSessionConnection.transportControls?.playFromMediaId(
            broadcast.broadcastId.toString(),
            Bundle().apply {
                putInt("SHOW_ID", show.id)
                putString("TYPE", PlaybackType.ARCHIVE.type)
            }
        )

        mediaSessionConnection.isLiveNow.postValue(false)
        broadcastRepository.playingLiveBroadcast = false

        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcast)
        broadcastRepository.nowPlayingShowLiveData.postValue(show)
    }

    fun stopPlayback() {
        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) transportControls.stop() }
    }

    fun prepareLivePlayback() {
        val customAction = CustomAction(getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value,
            mediaSessionConnection)

        customAction.live()
    }

    fun jumpBack30Seconds() {
        val customAction = CustomAction(getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value,
            mediaSessionConnection)

        customAction.replay()
    }

    fun jumpForward30Seconds() {
        val customAction = CustomAction(getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value,
            mediaSessionConnection)

        customAction.forward()
    }

    fun isShowBroadcastLiveNow(show: ShowEntity, broadcast: BroadcastEntity?): Boolean{
        return isLiveNow.value == null ||
            broadcast == null ||
                TimeHelper.isShowBroadcastLive(show, broadcast)
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
        track?.let {
            openBrowser(view, "$DISCOGS_SEARCH_URL${track.artist} ${track.song}$DISCOGS_QUERYSTRING")
        }
    }

    fun onClickDiscogs(view: View, topMusic: TopMusicEntity?) {
        topMusic?.let {
            openBrowser(view, "$DISCOGS_SEARCH_URL${topMusic.artist} ${topMusic.album}$DISCOGS_QUERYSTRING")
        }
    }

    fun onClickYoutube(view: View, track: TrackEntity?) {
        track?.let {
            openBrowser(view, "$YOUTUBE_SEARCH_URL${track.artist} ${track.song}$YOUTUBE_QUERYSTRING")
        }
    }

    fun onClickYoutube(view: View, topMusic: TopMusicEntity?) {
        topMusic?.let {
            openBrowser(view, "$YOUTUBE_SEARCH_URL${topMusic.artist} ${topMusic.album}$YOUTUBE_QUERYSTRING")
        }
    }

    fun onClickSpotifyNoApp(view: View, spotifyUri: String?) {
        val url = makeSpotifyUrl(spotifyUri ?: "")
        if (url.isNotEmpty())
            openBrowser(view, url)
    }

    fun openSpotify(view: View, spotifyData: SpotifyData?) {
        spotifyData?.let {
            openSpotify(view, it.uri)
        }
    }

    fun openSpotify(view: View, spotifyUri: String?) {
        if (isSpotifyInstalledOnDevice(view))
            openSpotifyApp(view, spotifyUri)
        else
            onClickSpotifyNoApp(view, spotifyUri)
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

    private fun isSpotifyInstalledOnDevice(view: View): Boolean {
        var isSpotifyInstalled = false

        try {
            view.context.packageManager.getPackageInfo("com.spotify.music", 0)
            isSpotifyInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {}

        return isSpotifyInstalled
    }

    // endregion

    // region Fetch

    fun fetchThirdPartyDataForTopMusic(topMusic: TopMusicEntity, topMusicRepository: TopMusicRepository) {
        if (topMusic.hasScrapedMetadata()) return

        var mbData: MusicBrainzReleaseData? = null
        var mbImageHref: String? = null
        var spotifyData: SpotifyData? = null
        
        launch {
            val musicBrainzJob = launch {
                mbData = MusicBrainz.searchFromAlbum(topMusic.album, topMusic.artist)
                mbImageHref = MusicBrainz.getCoverArtImage(mbData.id)
            }

            val spotifyJob = launch {
                val query = Spotify.getAlbumQuery(topMusic.album, topMusic.artist)
                spotifyData = Spotify.search(query)
            }

            musicBrainzJob.join()
            spotifyJob.join()

            val album = if (!mbData.album.isNullOrBlank()) mbData.album else spotifyData?.album
            val year = mbData.year ?: spotifyData?.year
            val imageHref = if (!mbImageHref.isNullOrBlank()) mbImageHref else spotifyData?.imageHref

            if (!album.isNullOrBlank()) {
                launch { topMusicRepository.updateTopMusicAlbum(topMusic.topMusicId, album)}
            }

            if (year != null) {
                launch { topMusicRepository.updateTopMusicYear(topMusic.topMusicId, year)}
            }

            if (!mbData.label.isNullOrBlank()) {
                launch { topMusicRepository.updateTopMusicLabel(topMusic.topMusicId, mbData.label)}
            }

            if (!imageHref.isNullOrBlank()) {
                launch { topMusicRepository.updateTopMusicImageHref(topMusic.topMusicId, imageHref)}
            }

            mbData?.let {
                launch { topMusicRepository.updateTopMusicMusicBrainzData(topMusic.topMusicId, mbData)}
            }

            spotifyData?.let {
                launch { topMusicRepository.updateTopMusicSpotifyData(topMusic.topMusicId, spotifyData)}
            }
        }
    }

    // endregion

    // region UI

    fun onClickFavorite(view: View, trackId: Int) {
        val imageView = view as? ImageView

        if (imageView?.tag == 0) {
            imageView.setImageResource(R.drawable.ic_favorite_white_24dp)
            imageView.tag = 1
            launch { favoriteDao.insert(FavoriteEntity(0, trackId)) }
        } else if (imageView?.tag == 1) {
            imageView.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            imageView.tag = 0
            launch { favoriteDao.deleteByTrackId(trackId) }
        }
    }

    fun onClickSubscribe(imageView: ImageView, showId: Int) {
        if (imageView.tag == 0) {
            imageView.setImageResource(R.drawable.ic_star_border_white_24dp)
            imageView.tag = 1
            launch { subscriptionDao.insert(SubscriptionEntity(0, showId)) }
        } else if (imageView.tag == 1) {
            imageView.setImageResource(R.drawable.ic_star_white_24dp)
            imageView.tag = 0
            launch { subscriptionDao.deleteByShowId(showId) }
        }
    }

    // endregion
}
