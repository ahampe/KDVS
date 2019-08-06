package fho.kdvs.global

import android.app.Application
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.Constants.READ_REQUEST_CODE
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs.DISCOGS_QUERYSTRING
import fho.kdvs.global.util.URLs.DISCOGS_SEARCH_URL
import fho.kdvs.global.util.URLs.YOUTUBE_QUERYSTRING
import fho.kdvs.global.util.URLs.YOUTUBE_SEARCH_URL
import fho.kdvs.global.web.*
import fho.kdvs.schedule.QuarterYear
import fho.kdvs.services.CustomAction
import fho.kdvs.services.LiveShowUpdater
import fho.kdvs.services.MediaSessionConnection
import fho.kdvs.services.PlaybackType
import fho.kdvs.show.ShowRepository
import fho.kdvs.show.TopMusicRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
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
    private val mediaSessionConnection: MediaSessionConnection,
    private val kdvsPreferences: KdvsPreferences
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
    val scrapedTracksForBroadcast= mutableListOf<TrackEntity>()

    fun updateLiveShows() = liveShowUpdater.beginUpdating()

    /** Signals the [ShowRepository] to scrape the schedule grid. */
    fun fetchShows() = showRepository.scrapeSchedule()

    fun getCurrentQuarterYear() : LiveData<QuarterYear> =
        showRepository.getCurrentQuarterYear()


    // region playback

    fun navigateToPlayer(navController: NavController) {
        navController.navigate(R.id.playerFragment)
    }

    fun playOrPausePlayback(activity: FragmentActivity?) {
        if (kdvsPreferences.offlineMode == true) {
            makeOfflineModeToast(activity)
            return
        }

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

    fun playLiveShowFromHome(activity: FragmentActivity?) {
        if (kdvsPreferences.offlineMode == true) {
            makeOfflineModeToast(activity)
            return
        }

        broadcastRepository.playingLiveBroadcast = true
        broadcastRepository.nowPlayingShowLiveData.postValue(showRepository.liveShowLiveData.value)
        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcastRepository.liveBroadcastLiveData.value)
        prepareLivePlayback()
    }

    fun playPastBroadcast(broadcast: BroadcastEntity, show: ShowEntity, activity: FragmentActivity?) {
        val file = getDestinationFileForBroadcast(broadcast, show)

        when (file.exists()) {
            true -> {
                try {
                    mediaSessionConnection.transportControls?.playFromUri(
                        Uri.fromFile(file),
                        Bundle().apply {
                            putInt("SHOW_ID", show.id)
                            putString("TYPE", PlaybackType.ARCHIVE.type)
                        }
                    )
                } catch (e: Exception) {
                    Timber.d("Error with URI playback: $e")
                    Toast.makeText(activity as? MainActivity,
                        "Error playing downloaded broadcast. Try re-downloading.",
                        Toast.LENGTH_SHORT)
                        .show()
                    return
                }

            }
            false -> {
                if (kdvsPreferences.offlineMode == true) {
                    makeOfflineModeToast(activity)
                    return
                }

                try {
                    mediaSessionConnection.transportControls?.playFromMediaId(
                        broadcast.broadcastId.toString(),
                        Bundle().apply {
                            putInt("SHOW_ID", show.id)
                            putString("TYPE", PlaybackType.ARCHIVE.type)
                        }
                    )
                } catch (e: Exception) {
                    Timber.d("Error with stream playback: $e")
                    Toast.makeText(activity as? MainActivity,
                        "Error streaming broadcast. Try again later.",
                        Toast.LENGTH_SHORT)
                        .show()
                    return
                }
            }
        }

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

    fun makeOfflineModeToast(activity: FragmentActivity?) {
        Toast.makeText(activity as? MainActivity,
            "Offline mode prevents live streaming.",
            Toast.LENGTH_SHORT)
            .show()
    }

    // endregion

    // region activity

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

    fun openPhone(view: View, number: String?) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
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

    // region Download

    private val broadcastExtension = ".mp3"
    private val temporaryExtension = ".tmp"

    fun getBroadcastDownloadTitle(broadcast: BroadcastEntity, show: ShowEntity): String =
        "${show.name} (${TimeHelper.dateFormatter.format(broadcast.date)})"

    fun getDestinationFile(filename: String): File {
        val folder = getDownloadFolder()
        return File(Uri.parse("${folder?.absolutePath}/$filename").path)
    }

    fun getDownloadFolder(): File? {
        if (isExternalStorageWritable()) {
            return if (!kdvsPreferences.downloadPath.isNullOrBlank())
                File(kdvsPreferences.downloadPath)
            else
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "KDVS")
        }

        return null
    }

    fun setDownloadFolder(activity: FragmentActivity?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        activity?.startActivityForResult(intent, READ_REQUEST_CODE)
    }

    fun getDownloadingFilename(title: String) = "$title$broadcastExtension$temporaryExtension"

    /** Rename '.mp3.tmp' to '.mp3' */
    fun renameFileAfterCompletion(file: File) {
        val dest = File("${file.parent}/${file.nameWithoutExtension}")
        file.renameTo(dest)


        // TODO: embed metadata in mp3
    }

    fun makeDownloadRequest(url: String, title: String, file: File): DownloadManager.Request {
        return DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(file))
            .setAllowedOverMetered(kdvsPreferences.offlineMode != true)
            .setAllowedOverRoaming(kdvsPreferences.offlineMode != true)
    }

    fun deleteFile(file: File) {
        try {
            file.delete()
        } catch (e: Exception) {
            Timber.d("File deletion failed ${file.name}")
        }
    }

    fun isBroadcastDownloaded(broadcast: BroadcastEntity, show: ShowEntity): Boolean {
        val folder = getDownloadFolder()
        val title = getBroadcastDownloadTitle(broadcast, show)
        val files = folder?.listFiles()

        files?.let {
            return (it.count{ f -> f.name == "$title$broadcastExtension" } > 0)
        }

        return false
    }

    private fun getDestinationFileForBroadcast(broadcast: BroadcastEntity, show: ShowEntity): File {
        val filename = getBroadcastDownloadTitle(broadcast, show) + broadcastExtension
        return getDestinationFile(filename)
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // endregion

    // region fetch

    // TODO: multiple attempts?
    fun fetchThirdPartyDataForTopMusic(topMusic: TopMusicEntity, topMusicRepository: TopMusicRepository) {
        if (topMusic.hasScrapedMetadata() || kdvsPreferences.offlineMode == true)
            return

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

    // TODO: launch all fetches here
    fun refreshData() {
        launch {
            fetchShows()
        }
    }

    // endregion

    // region ui

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
