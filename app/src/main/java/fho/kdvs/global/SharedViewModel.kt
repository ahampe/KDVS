package fho.kdvs.global

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import fho.kdvs.R
import fho.kdvs.api.endpoint.SpotifyEndpoint
import fho.kdvs.api.service.MusicBrainzService
import fho.kdvs.api.service.SpotifyService
import fho.kdvs.api.service.YouTubeService
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.dialog.BinaryChoiceDialogFragment
import fho.kdvs.fundraiser.FundraiserRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.database.joins.ShowTimeslotsJoin
import fho.kdvs.global.enums.ThirdPartyService
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.RequestCodes
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.global.util.URLs.DISCOGS_QUERYSTRING
import fho.kdvs.global.util.URLs.DISCOGS_SEARCH_URL
import fho.kdvs.global.util.URLs.YOUTUBE_QUERYSTRING
import fho.kdvs.global.util.URLs.YOUTUBE_SEARCH_URL
import fho.kdvs.news.NewsRepository
import fho.kdvs.schedule.QuarterRepository
import fho.kdvs.schedule.QuarterYear
import fho.kdvs.services.*
import fho.kdvs.services.notification.CustomAction
import fho.kdvs.services.notification.PlaybackType
import fho.kdvs.show.ShowRepository
import fho.kdvs.staff.StaffRepository
import fho.kdvs.subscription.SubscriptionRepository
import fho.kdvs.topmusic.TopMusicRepository
import fho.kdvs.track.BroadcastTrackDetailsFragmentDirections
import fho.kdvs.track.FavoriteTrackDetailsFragmentDirections
import fho.kdvs.track.TrackDetailsType
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.math.min

const val BROADCAST_EXT = ".mp3"
const val TEMP_EXT = ".download"
const val DOWNLOAD_CHILD = "KDVS"

/** An [AndroidViewModel] scoped to the main activity.
 * Use this for data that will be consumed in many places. */
class SharedViewModel @Inject constructor(
    application: Application,
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val newsRepository: NewsRepository,
    private val staffRepository: StaffRepository,
    private val fundraiserRepository: FundraiserRepository,
    private val topMusicRepository: TopMusicRepository,
    private val quarterRepository: QuarterRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val trackRepository: TrackRepository,
    private val favoriteTrackDao: FavoriteTrackDao,
    private val favoriteBroadcastDao: FavoriteBroadcastDao,
    private val subscriptionDao: SubscriptionDao,
    private val liveShowUpdater: LiveShowUpdater,
    private val mediaSessionConnection: MediaSessionConnection,
    private val kdvsPreferences: KdvsPreferences,
    private val spotifyService: SpotifyService,
    private val youTubeService: YouTubeService,
    private val musicBrainzService: MusicBrainzService
) : BaseViewModel(application) {

    val liveStreamLiveData: MediatorLiveData<Pair<ShowTimeslotEntity, BroadcastEntity?>>
        get() = showRepository.liveStreamLiveData

    val nowPlayingStreamLiveData: MediatorLiveData<Pair<ShowTimeslotEntity, BroadcastEntity>>
        get() = showRepository.nowPlayingStreamLiveData

    val nowPlayingShow: LiveData<ShowTimeslotEntity>
        get() = broadcastRepository.nowPlayingShowLiveData

    val liveShow: LiveData<ShowTimeslotEntity>
        get() = showRepository.liveShowLiveData

    val nextLiveShow: LiveData<ShowTimeslotEntity>
        get() = showRepository.nextShowLiveData

    val nowPlayingBroadcast: LiveData<BroadcastEntity>
        get() = broadcastRepository.nowPlayingBroadcastLiveData

    val liveBroadcast: LiveData<BroadcastEntity>
        get() = broadcastRepository.liveBroadcastLiveData

    val spotToken = MutableLiveData<String>()

    val isPlayingAudioNow = mediaSessionConnection.playbackState

    val isLiveNow: LiveData<Boolean?> = showRepository.isLiveNow

    /** Use across various lifecycles of PlayerFragment to maintain list of scraped tracks for live broadcast. */
    val scrapedTracksForBroadcast = mutableListOf<TrackEntity>()

    fun updateLiveShows() = liveShowUpdater.beginUpdating()

    fun getBroadcastRepo() = broadcastRepository

    fun getCurrentQuarterYear(): LiveData<QuarterYear> =
        showRepository.getCurrentQuarterYear()

    // region show

    /**
     * Returns a [LiveData] wrapping a list of [TimeslotEntity] objects associated with a given
     * [Show]'s ID.
     */
    fun getTimeslots(show: Show) = showRepository.timeslotsById(show.id)

    /**
     * Returns a [LiveData] wrapping the [ShowTimeslotsJoin] associated with a given [Show]'s ID.
     */
    fun getShowTimeslotsJoin(show: Show) = showRepository.showTimeslotsJoinById(show.id)

    // endregion

    // region quarter

    /** All quarter-years in the database. */
    val allQuarterYearsLiveData = quarterRepository.allQuarterYearsLiveData

    /** The current real quarter-year */
    private val currentQuarterYearLiveData = showRepository.getCurrentQuarterYear()

    /** The currently selected quarter-year. */
    private val selectedQuarterYearLiveData = quarterRepository.selectedQuarterYearLiveData

    /** Sets the given [QuarterYear]. Change will be reflected in [selectedQuarterYearLiveData]. */
    fun selectQuarterYear(quarterYear: QuarterYear) =
        quarterRepository.selectQuarterYear(quarterYear)

    /**
     * Gets the selected [QuarterYear] if there is one, else returns the most recent [QuarterYear].
     * If both are null, returns null.
     *
     * Note: It's important that [allQuarterYearsLiveData] has observers, otherwise it will not be updated.
     * This doesn't hold for [selectedQuarterYearLiveData] as it's not sourced from RxJava.
     */
    fun loadQuarterYear(): QuarterYear? =
        selectedQuarterYearLiveData.value ?: allQuarterYearsLiveData.value?.firstOrNull()

    /**
     * Code to execute when a new current QuarterYear is observed.
     */
    fun onNewQuarter(context: Context?) {
        processSubscriptionsOnQuarterChange()
        makeNewQuarterToast(context)
    }

    private fun makeNewQuarterToast(context: Context?) =
        Toast.makeText(context, "The new quarter has begun!", Toast.LENGTH_SHORT)
            .show()

    private fun getCurrentQuarterShowTimeslots(): List<ShowTimeslotEntity>? {
        currentQuarterYearLiveData.value?.let {
            return showRepository.getShowsByQuarterYear(it)
        }

        return null
    }

    private fun getCurrentQuarterShowTimeslotsJoins(): List<ShowTimeslotsJoin>? {
        currentQuarterYearLiveData.value?.let {
            return showRepository.getShowTimeslotsJoinsByQuarterYear(it)
        }

        return null
    }

    // endregion

    // region playback

    fun navigateToPlayer(navController: NavController) {
        navController.navigate(R.id.playerFragment)
    }

    fun playOrPausePlayback(activity: FragmentActivity) {
        if (kdvsPreferences.offlineMode == true) {
            makeOfflineModeToast(activity)
            return
        }

        var hasFired = false

        if (mediaSessionConnection.playbackState.value?.isPrepared == false) {
            nowPlayingStreamLiveData.observe(
                activity,
                Observer { (nowPlayingShow, nowPlayingBroadcast) ->
                    if (hasFired) return@Observer

                    hasFired = true

                    if (isShowBroadcastLiveNow(
                            nowPlayingShow,
                            nowPlayingBroadcast
                        )
                    ) {
                        goLive()
                    } else {
                        preparePastBroadcastForPlaybackAndPlay(
                            nowPlayingBroadcast,
                            nowPlayingShow,
                            activity
                        )
                    }
                })
        }

        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) {
                transportControls.pause()
            } else {
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
        goLive()
    }

    fun preparePastBroadcastForPlaybackAndPlay(
        broadcast: BroadcastEntity,
        show: ShowTimeslotEntity,
        activity: FragmentActivity
    ) {
        preparePastBroadcastForPlayback(broadcast, show, activity)
        playPastBroadcast(broadcast, show)
    }

    fun preparePastBroadcastForPlayback(
        broadcast: BroadcastEntity,
        show: ShowTimeslotEntity,
        activity: FragmentActivity
    ) {
        val file = getDestinationFileForBroadcast(broadcast, show)

        when (file?.exists()) {
            true -> {
                try {
                    mediaSessionConnection.transportControls?.prepareFromUri(
                        Uri.fromFile(file),
                        makeMediaSessionExtras(show)
                    )
                } catch (e: Exception) {
                    Timber.e("Error with URI playback: $e")
                    Toast.makeText(
                        activity as? MainActivity,
                        "Error playing downloaded broadcast. Try re-downloading.",
                        Toast.LENGTH_SHORT
                    )
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
                    mediaSessionConnection.transportControls?.prepareFromMediaId(
                        broadcast.broadcastId.toString(),
                        makeMediaSessionExtras(show)
                    )
                } catch (e: Exception) {
                    Timber.e("Error with stream playback: $e")
                    Toast.makeText(
                        activity as? MainActivity,
                        "Error streaming broadcast. Try again later.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }
            }
        }
    }

    private fun makeMediaSessionExtras(show: ShowTimeslotEntity) = Bundle().apply {
        putInt("SHOW_ID", show.id)

        kdvsPreferences.lastPlayedBroadcastId?.let {
            putLong(
                "POSITION",
                kdvsPreferences.lastPlayedBroadcastPosition ?: 0L
            )
        }

        putString("TYPE", PlaybackType.ARCHIVE.type)
    }

    private fun playPastBroadcast(broadcast: BroadcastEntity, show: ShowTimeslotEntity) {
        mediaSessionConnection.transportControls?.play()

        mediaSessionConnection.isLiveNow.postValue(false)
        broadcastRepository.playingLiveBroadcast = false

        broadcastRepository.nowPlayingBroadcastLiveData.postValue(broadcast)
        broadcastRepository.nowPlayingShowLiveData.postValue(show)
    }

    fun stopPlayback() {
        val transportControls = mediaSessionConnection.transportControls ?: return
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            if (playbackState.isPlaying) transportControls.stop()
        }
    }

    fun goLive() {
        val customAction = CustomAction(
            getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value
        )

        customAction.live()
    }

    fun jumpBack30Seconds() {
        val customAction = CustomAction(
            getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value
        )

        customAction.replay()
    }

    fun jumpForward30Seconds() {
        val customAction = CustomAction(
            getApplication(),
            mediaSessionConnection.transportControls,
            mediaSessionConnection.playbackState.value
        )

        customAction.forward()
    }

    fun isShowBroadcastLiveNow(show: ShowTimeslotEntity, broadcast: BroadcastEntity?): Boolean {
        return isLiveNow.value == null ||
                broadcast == null ||
                TimeHelper.isShowBroadcastLive(show, broadcast)
    }

    fun makeOfflineModeToast(activity: FragmentActivity?) {
        Toast.makeText(
            activity as? MainActivity,
            "Offline mode prevents live streaming.",
            Toast.LENGTH_SHORT
        )
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

    fun openBrowser(context: Context, url: String?) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    fun openPhone(view: View, number: String?) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        if (intent.resolveActivity(view.context.packageManager) != null) {
            startActivity(view.context, intent, null)
        }
    }

    fun onClickDiscogs(context: Context, track: TrackEntity?) {
        track?.let {
            openBrowser(
                context,
                "$DISCOGS_SEARCH_URL${track.artist} ${track.song}$DISCOGS_QUERYSTRING"
            )
        }
    }

    fun onClickDiscogs(context: Context, topMusic: TopMusicEntity?) {
        topMusic?.let {
            openBrowser(
                context,
                "$DISCOGS_SEARCH_URL${topMusic.artist} ${topMusic.album}$DISCOGS_QUERYSTRING"
            )
        }
    }

    fun onClickYoutube(context: Context, track: TrackEntity?) {
        track?.let {
            openBrowser(context, makeYoutubeQuery(track))
        }
    }

    fun onClickYoutube(context: Context, topMusic: TopMusicEntity?) {
        topMusic?.let {
            openBrowser(context, makeYoutubeQuery(topMusic))
        }
    }

    /**
     * Opens browser to URL of temporary YouTube playlist generated by concatenating list of
     * video IDs (max URL size: 2048 chars). If user has YouTube app installed, they'll be redirected
     * automatically from their browser to the app.
     */
    fun exportVideosToYouTubePlaylist(context: Context, ids: List<String>) {
        val url = makeYouTubePlaylist(ids)
        openBrowser(context, url)
    }

    private fun makeYouTubePlaylist(ids: List<String>) =
        ("https://www.youtube.com/watch_videos?video_ids=" + ids.joinToString(",")).let {
            it.substring(0, min(it.length, 2048))
                .substringBeforeLast(",")
        }

    private fun makeYoutubeQuery(topMusic: TopMusicEntity?) =
        "$YOUTUBE_SEARCH_URL${topMusic?.artist} ${topMusic?.album}$YOUTUBE_QUERYSTRING".urlEncoded

    private fun makeYoutubeQuery(track: TrackEntity?) =
        "$YOUTUBE_SEARCH_URL${track?.artist} ${track?.song}$YOUTUBE_QUERYSTRING".urlEncoded

    private fun openYouTubeApp(context: Context, uri: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uri)
            putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(context, intent, null)
        }
    }

    private fun onClickSpotifyNoApp(context: Context, spotifyUri: String) {
        val url = makeSpotifyUrl(spotifyUri)
        if (url.isNotEmpty())
            openBrowser(context, url)
    }

    fun openSpotify(context: Context, spotifyUri: String) {
        if (isSpotifyInstalledOnDevice(context))
            openSpotifyApp(context, spotifyUri)
        else
            onClickSpotifyNoApp(context, spotifyUri)
    }

    /** Launches Spotify login activity. */
    fun loginSpotify(activity: FragmentActivity) {
        val builder = AuthenticationRequest.Builder(
            SpotifyEndpoint.SPOTIFY_CLIENT_ID,
            AuthenticationResponse.Type.TOKEN,
            SpotifyEndpoint.SPOTIFY_REDIRECT_URI
        )

        builder.setScopes(
            arrayOf(
                "playlist-modify-public",
                "playlist-read-private",
                "playlist-modify-private"
            )
        )

        val request = builder.build()

        AuthenticationClient.openLoginActivity(activity, RequestCodes.SPOTIFY_LOGIN, request)
    }

    fun isSpotifyAuthVoidOrExpired() = kdvsPreferences.spotifyAuthToken.isNullOrEmpty() ||
            kdvsPreferences.spotifyLastLogin ?: 0 < TimeHelper.getOneHourAgoUTC().toEpochSecond()

    private fun openSpotifyApp(context: Context, spotifyUri: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(spotifyUri)
            putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(context, intent, null)
        } else {
            onClickSpotifyNoApp(context, spotifyUri)
        }
    }

    fun isSpotifyInstalledOnDevice(context: Context): Boolean {
        var isSpotifyInstalled = false

        try {
            context.packageManager.getPackageInfo("com.spotify.music", 0)
            isSpotifyInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {
        }

        return isSpotifyInstalled
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

    // region Download

    /**
     * Observes a file in download folder corresponding to given filename, and calls function(s) on
     * completion or deletion.
     */
    fun callOnFileEventForFilename(
        filename: String,
        onComplete: () -> Unit,
        onDelete: (() -> Unit)?
    ) {
        val folder = getDownloadFolder()
        val downloadingFilename = getDownloadingFilename(filename)

        val observer = object : FileObserver(folder?.path) {
            override fun onEvent(event: Int, filename: String?) {
                filename?.let {
                    if (it == downloadingFilename) {
                        when (event) {
                            MOVED_FROM -> {
                                onComplete()
                            }
                            DELETE -> {
                                onDelete?.let { it() }
                            }
                        }
                    }
                }
            }
        }

        observer.startWatching()
    }

    fun downloadBroadcast(
        activity: FragmentActivity,
        broadcast: BroadcastEntity?,
        show: Show?,
        folder: File?
    ): Boolean {
        if (kdvsPreferences.offlineMode == true) {
            makeOfflineModeToast(activity)
            return false
        }

        if (broadcast == null || show == null || folder == null ||
            isBroadcastDownloaded(
                broadcast,
                show
            )
        )
            return false

        val title = getBroadcastDownloadTitle(broadcast, show)
        val filename = getDownloadingFilename(title)

        (activity as? MainActivity)?.let { mainActivity ->
            if (mainActivity.isStoragePermissionGranted()) {
                val url = URLs.archiveForBroadcast(broadcast)

                url?.let {
                    if (!folder.exists())
                        folder.mkdirs()

                    try {
                        val request = makeDownloadRequest(url, title, filename)

                        val downloadManager =
                            mainActivity.applicationContext?.getSystemService(Context.DOWNLOAD_SERVICE)
                                    as DownloadManager

                        downloadManager.enqueue(request)

                        mainActivity.applicationContext?.runOnUiThread {
                            Toast.makeText(
                                this,
                                "Download Started",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        return true
                    } catch (e: Exception) {
                        Timber.e("Error downloading broadcast: ${e.message}")

                        mainActivity.applicationContext?.runOnUiThread {
                            Toast.makeText(
                                this,
                                "Error downloading broadcast",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        return false
                    }
                }
            }
        }

        return false
    }

    fun deleteBroadcast(broadcast: BroadcastEntity, show: Show) {
        getDownloadFileForBroadcast(broadcast, show)?.let {
            deleteFile(it)
        }
    }

    /** Must escape reserved system chars when writing a file. */
    fun getBroadcastDownloadTitle(broadcast: BroadcastEntity, show: Show): String {
        val reservedChars = """[?:"*|/\\<>]""".toRegex()

        val escapedName = show.name?.replace(reservedChars, "_")

        return "$escapedName (${TimeHelper.dateFormatter.format(broadcast.date)})"
    }

    fun getBroadcastDownloadUiTitle(broadcast: BroadcastEntity, show: Show): String =
        "${show.name} (${TimeHelper.uiDateFormatter.format(broadcast.date)})"

    private fun getDownloadFileForBroadcast(broadcast: BroadcastEntity, show: Show): File? =
        getFileInDownloadFolder(getDownloadedFilename(getBroadcastDownloadTitle(broadcast, show)))

    private fun getFileInDownloadFolder(filename: String): File {
        val folder = getDownloadFolder()
        return File(Uri.parse("${folder?.absolutePath}${File.separator}$filename").path)
    }

    fun getDownloadFolder(): File? {
        return if (isExternalStorageWritable()) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                DOWNLOAD_CHILD
            )
        } else null
    }

    private fun getDownloadedFilename(title: String) = "$title$BROADCAST_EXT"

    fun getDownloadingFilename(title: String) = "$title$BROADCAST_EXT$TEMP_EXT"

    fun removeExtension(src: File): Boolean {
        if (src.exists()) {
            val extensionIndex = src.path.lastIndexOf('.')

            if (extensionIndex > 0) {
                val destPath = src.path.substring(0, extensionIndex)

                if (destPath.isNotBlank())
                    return src.renameTo(File(destPath))
            }
        }

        return false
    }

    fun makeDownloadRequest(url: String, title: String, filename: String): DownloadManager.Request {
        return DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_MUSIC,
                "${File.separator}$DOWNLOAD_CHILD${File.separator}$filename"
            )
            .setAllowedOverMetered(true) // TODO: make preference
            .setAllowedOverRoaming(true)
    }

    fun deleteFile(file: File) {
        try {
            file.delete()
        } catch (e: Exception) {
            Timber.d("File deletion failed ${file.name}")
        }
    }

    fun isBroadcastDownloaded(broadcast: BroadcastEntity, show: Show): Boolean {
        val folder = getDownloadFolder()
        val title = getBroadcastDownloadTitle(broadcast, show)
        val files = folder?.listFiles()

        files?.let {
            return (it.count { f -> f.name == getDownloadedFilename(title) } > 0)
        }

        return false
    }

    fun isBroadcastDownloading(broadcast: BroadcastEntity, show: Show): Boolean {
        val folder = getDownloadFolder()
        val title = getBroadcastDownloadTitle(broadcast, show)
        val files = folder?.listFiles()

        files?.let {
            return (it.count { f -> f.name == getDownloadingFilename(title) } > 0)
        }

        return false
    }

    private fun getDestinationFileForBroadcast(
        broadcast: BroadcastEntity,
        show: ShowTimeslotEntity
    ): File? {
        val filename = getBroadcastDownloadTitle(broadcast, show) + BROADCAST_EXT
        return getFileInDownloadFolder(filename)
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // endregion

    // region fetch

    /** Signals the [ShowRepository] to scrape the schedule grid. */
    fun fetchShows() = showRepository.scrapeSchedule()

    /** Forces the [ShowRepository] to scrape the schedule grid. */
    private fun forceFetchShows() = showRepository.forceScrapeSchedule()

    /** Forces the [NewsRepository] to scrape the news page(s). */
    private fun forceFetchNewsArticles() = newsRepository.forceScrapeNews()

    /** Forces the [TopMusicRepository] to scrape the top music pages. */
    private fun forceFetchTopMusicItems() = topMusicRepository.forceScrapeTopMusic()

    /** Forces the [StaffRepository] to scrape the staff page. */
    private fun forceFetchStaff() = staffRepository.forceScrapeStaff()

    /** Forces the [FundraiserRepository] to scrape the fundraiser page. */
    private fun forceFetchFundraiser() = fundraiserRepository.forceScrapeFundraiser()

    // TODO: multiple attempts?
    fun fetchThirdPartyDataForTopMusic(
        topMusic: TopMusicEntity,
        topMusicRepository: TopMusicRepository
    ) {
        if (topMusic.hasThirdPartyInfo || kdvsPreferences.offlineMode == true) return

        launch {
            val spotifyFind = spotifyService.findAlbumAsync(topMusic.album, topMusic.artist)
            val musicBrainzFind =
                musicBrainzService.findAlbumsAsync(topMusic.album, topMusic.artist)
            val youTubeFind = youTubeService.findVideoAsync(topMusic.artist, topMusic.album)

            val spotAlbum = spotifyFind.await()
            val mbAlbum = musicBrainzFind.await()

            // We must make another API request to obtain tracks
            val spotifyGet = spotifyService.getAlbumFromIDAsync(spotAlbum?.id)
            val spotTracks = spotifyGet.await()?.tracks

            val mbAlbumHref = mbAlbum?.id?.let { id ->
                musicBrainzService.getAlbumArtHrefAsync(id).await()
            }

            val album = mbAlbum?.name ?: spotAlbum?.name
            val year = mbAlbum?.year ?: spotAlbum?.year
            val label = mbAlbum?.label
            val imageHref = mbAlbumHref ?: spotAlbum?.imageHref
            val uri = spotAlbum?.uri
            val spotTrackUris = spotTracks?.mapNotNull { t -> t?.uri }

            album?.let {
                topMusicRepository.updateTopMusicAlbum(topMusic.topMusicId, it)
            }

            year?.let {
                topMusicRepository.updateTopMusicYear(topMusic.topMusicId, it)
            }

            label?.let {
                topMusicRepository.updateTopMusicLabel(topMusic.topMusicId, it)
            }

            imageHref?.let {
                topMusicRepository.updateTopMusicImageHref(topMusic.topMusicId, it)
            }

            uri?.let {
                topMusicRepository.updateTopMusicSpotifyAlbumUri(topMusic.topMusicId, it)
            }

            spotTrackUris?.let {
                topMusicRepository.updateTopMusicSpotifyTrackUris(topMusic.topMusicId, it)
            }

            val youTubeVideo = youTubeFind.await()
            val youTubeId = youTubeVideo?.id

            youTubeId?.let {
                topMusicRepository.updateTopMusicYouTubeId(topMusic.topMusicId, it)
            }

            if (listOfNotNull(album, year, label, imageHref, youTubeId).isNotEmpty()) {
                topMusicRepository.updateHasThirdPartyInfo(topMusic.topMusicId, true)
            }
        }
    }

    fun fetchThirdPartyDataForTrack(track: TrackEntity): Job? {
        if (track.hasThirdPartyInfo || kdvsPreferences.offlineMode == true) return null

        return launch {
            val spotifyAlbumFind = spotifyService.findAlbumAsync(track.album, track.artist)
            val spotifyTrackFind = spotifyService.findTrackAsync(track.song, track.artist)
            val musicBrainzFind = musicBrainzService.findAlbumsAsync(track.album, track.artist)
            val youTubeFind = youTubeService.findVideoAsync(track.artist, track.song)

            val spotAlbum = spotifyAlbumFind.await()
            val spotTrack = spotifyTrackFind.await()
            val mbAlbum = musicBrainzFind.await()

            val mbAlbumHref = mbAlbum?.id?.let { id ->
                musicBrainzService.getAlbumArtHrefAsync(id).await()
            }

            val album = mbAlbum?.name ?: spotAlbum?.name
            val year = mbAlbum?.year ?: spotAlbum?.year
            val label = mbAlbum?.label
            val imageHref = mbAlbumHref ?: spotAlbum?.imageHref
            val albumUri = spotAlbum?.uri
            val trackUri = spotTrack?.uri

            album?.let {
                trackRepository.updateTrackAlbum(track.trackId, it)
            }

            year?.let {
                trackRepository.updateTrackYear(track.trackId, it)
            }

            label?.let {
                trackRepository.updateTrackLabel(track.trackId, it)
            }

            imageHref?.let {
                trackRepository.updateTrackImageHref(track.trackId, imageHref)
            }

            albumUri?.let {
                trackRepository.updateSpotifyAlbumUri(track.trackId, albumUri)
            }

            trackUri?.let {
                trackRepository.updateSpotifyTrackUri(track.trackId, trackUri)
            }

            val youTubeVideo = youTubeFind.await()
            val youTubeId = youTubeVideo?.id

            youTubeId?.let {
                trackRepository.updateTrackYouTubeId(track.trackId, it)
            }

            if (listOfNotNull(
                    album,
                    year,
                    label,
                    imageHref,
                    albumUri,
                    trackUri,
                    youTubeId
                ).isNotEmpty()
            ) {
                trackRepository.updateHasThirdPartyInfo(track.trackId, true)
            }
        }
    }

    fun refreshData() {
        launch {
            forceFetchShows()
            forceFetchNewsArticles()
            forceFetchTopMusicItems()
            forceFetchStaff()
            forceFetchFundraiser()
        }
    }

    // endregion

    // region ui

    fun onClickTrackFavorite(view: View, track: TrackEntity) {
        val imageView = view as? ImageView

        if (imageView?.tag == 0) {
            imageView.setImageResource(R.drawable.ic_favorite_white_24dp)
            imageView.tag = 1
            launch { favoriteTrackDao.insert(FavoriteTrackEntity(0, track.trackId)) }

            // Fetch third party data now to make entry into FavoriteFragment seamless
            launch { fetchThirdPartyDataForTrack(track) }
        } else if (imageView?.tag == 1) {
            imageView.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            imageView.tag = 0
            launch { favoriteTrackDao.deleteByTrackId(track.trackId) }
        }
    }

    fun onClickBroadcastFavorite(view: View, broadcast: BroadcastEntity) {
        val imageView = view as? ImageView

        if (imageView?.tag == 0) {
            imageView.setImageResource(R.drawable.ic_favorite_white_24dp)
            imageView.tag = 1
            launch {
                addBroadcastFavorite(broadcast)
            }
        } else if (imageView?.tag == 1) {
            imageView.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            imageView.tag = 0
            launch { favoriteBroadcastDao.deleteByBroadcastId(broadcast.broadcastId) }
        }
    }

    fun addBroadcastFavorite(broadcast: BroadcastEntity) {
        launch {
            favoriteBroadcastDao.insert(
                FavoriteBroadcastEntity(
                    0,
                    broadcast.broadcastId
                )
            )
        }
    }

    fun onClickSubscribe(imageView: ImageView, showWithTimeslots: ShowTimeslotsJoin, context: Context?) {
        showWithTimeslots.show?.let { show ->
            if (imageView.tag == 0) {
                subscribeToShowAndMakeToast(showWithTimeslots, context)

                imageView.setImageResource(R.drawable.ic_star_border_white_24dp)
                imageView.tag = 1
            } else if (imageView.tag == 1) {
                cancelSubscription(show)
                Toast.makeText(context, "Unsubscribed from ${show.name}", Toast.LENGTH_SHORT)
                    .show()

                imageView.setImageResource(R.drawable.ic_star_white_24dp)
                imageView.tag = 0
            }
        }
    }

    fun onClickTrackHeader(
        navController: NavController,
        view: View,
        track: TrackEntity,
        type: TrackDetailsType
    ) {
        val showId = view.tag as? Int?

        showId?.let {
            when (type) {
                TrackDetailsType.BROADCAST -> {
                    val navAction = BroadcastTrackDetailsFragmentDirections
                        .actionBroadcastTrackDetailsFragmentToBroadcastDetailsFragment(
                            showId,
                            track.broadcastId
                        )
                    if (navController.currentDestination?.id == R.id.broadcastTrackDetailsFragment)
                        navController.navigate(navAction)
                }
                TrackDetailsType.FAVORITE -> {
                    val navAction = FavoriteTrackDetailsFragmentDirections
                        .actionFavoriteTrackDetailsFragmentToBroadcastDetailsFragment(
                            showId,
                            track.broadcastId
                        )
                    if (navController.currentDestination?.id == R.id.favoriteTrackDetailsFragment)
                        navController.navigate(navAction)
                }
            }
        }
    }

    /** Method to generalize the process of exporting music to third-party playlists via dialog. */
    fun onClickExportIcon(
        fragment: Fragment,
        requestCode: Int,
        service: ThirdPartyService,
        count: Int? = null
    ) {
        if (count == 0) {
            Toast.makeText(
                fragment.context,
                "Sorry, there are no exportable items.",
                Toast.LENGTH_LONG
            )
                .show()

            return
        }

        val dialog = BinaryChoiceDialogFragment()
        val args = Bundle()

        args.putString("title", "Export")
        args.putString(
            "message",
            "Export${count
                ?: ""} ${if (count == 1) "track" else "tracks"} to a ${service.title} playlist?"
        )

        dialog.arguments = args
        dialog.setTargetFragment(fragment, requestCode)
        dialog.show(fragment.requireFragmentManager(), "tag")
    }

    // endregion

    // region alarm

    fun reRegisterAlarms() {
        val alarmMgr = KdvsAlarmManager(getApplication(), showRepository)

        launch {
            val subscribedShows = getSubscribedShowsWithTimeslots()

            subscribedShows.forEach {
                it.show?.let { show ->
                    alarmMgr.cancelShowAlarm(show)
                    alarmMgr.registerShowAlarmsAsync(it)
                }
            }

            Timber.d("Alarms reregistered")
        }
    }

    /**
     * When user changes the alarm notification window in settings, we'll need to re-register all alarms
     * with the new window. We must first cancel all alarms before updating the preference,
     * such that we can initialize matching Intents.
     */
    fun reRegisterAlarmsAndUpdatePreference(newWindow: Long?) {
        if (newWindow == null) return

        val alarmMgr = KdvsAlarmManager(getApplication(), showRepository)

        launch {
            val subscribedShows = getSubscribedShowsWithTimeslots()

            subscribedShows.forEach {
                it.show?.let { show ->
                    alarmMgr.cancelShowAlarm(show)
                }
            }

            kdvsPreferences.alarmNoticeInterval = newWindow

            subscribedShows.forEach {
                alarmMgr.registerShowAlarmsAsync(it)
            }

            Timber.d("Alarms reregistered")
        }
    }

    // endregion

    // region subscription
    /**
     * After a quarter change, subscribed recurring [ShowEntity]s will have new database objects,
     * and possibly new associated [TimeslotEntity]s, so we must cancel existing ones and insert
     * new ones based on matching show names in the current quarter.
     *
     * Nonrecurring [ShowEntity]s will simply have their subscriptions cancelled.
     */
    private fun processSubscriptionsOnQuarterChange() { // TODO: test more thoroughly
        launch {
            val subscribedShowsWithTimeslots = getSubscribedShowsWithTimeslots()

            val subscribedShows = subscribedShowsWithTimeslots.mapNotNull { s -> s.show }
            val recurringSubscribedShows = getRecurringShows(subscribedShows)
            val nonRecurringSubscribedShows = subscribedShows
                .filterNot { s -> recurringSubscribedShows?.contains(s) == true }

            recurringSubscribedShows?.forEach {
                subscribedShowsWithTimeslots
                    .firstOrNull { s -> s.show?.id == it.id }
                    ?.let {
                        updateRecurringSubscription(it)
                    }
            }

            nonRecurringSubscribedShows.forEach {
                cancelSubscription(it)
            }
        }
    }

    private fun updateRecurringSubscription(showWithTimeslots: ShowTimeslotsJoin) {
        showWithTimeslots.show?.let { show ->
            cancelSubscription(show)

            val currentShows = getCurrentQuarterShowTimeslotsJoins()
            val matchingShow = currentShows
                ?.firstOrNull { s -> s.show?.name == show.name }

            matchingShow?.let {
                subscribeToShowWithoutToast(showWithTimeslots)
            }
        }
    }

    private fun subscribeToShowWithoutToast(showWithTimeslots: ShowTimeslotsJoin) {
        launch {
            showWithTimeslots.show?.let { show ->
                val success = subscribeToShow(showWithTimeslots)

                if (success) {
                    launch { subscriptionDao.insert(SubscriptionEntity(0, show.id)) }
                }
            }
        }
    }

    private fun subscribeToShowAndMakeToast(showWithTimeslots: ShowTimeslotsJoin, context: Context?) {
        launch {
            showWithTimeslots.show?.let { show ->
                val success = subscribeToShow(showWithTimeslots)

                if (success) {
                    launch { subscriptionDao.insert(SubscriptionEntity(0, show.id)) }
                    context?.runOnUiThread {
                        Toast.makeText(context, "Subscribed to ${show.name}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private suspend fun subscribeToShow(showWithTimeslots: ShowTimeslotsJoin): Boolean {
        val alarmMgr = KdvsAlarmManager(getApplication(), showRepository)
        return alarmMgr.registerShowAlarmsAsync(showWithTimeslots).await()
    }

    private fun cancelSubscription(show: Show) {
        launch { subscriptionDao.deleteByShowId(show.id) }
        launch {
            val alarmMgr = KdvsAlarmManager(getApplication(), showRepository)
            alarmMgr.cancelShowAlarm(show)
        }
    }

    private fun getSubscribedShowsWithTimeslots(): List<ShowTimeslotsJoin> {
        return subscriptionRepository.subscribedShows()
    }

    /**
     * Takes in list of [Show]s and returns those from list that are present in current quarter,
     * based on show name.
     */
    private fun getRecurringShows(shows: List<Show>): List<Show>? {
        val currentShows = getCurrentQuarterShowTimeslots()

        currentShows?.let {
            return shows
                .filter { show ->
                    currentShows
                        .map { s -> s.name }
                        .contains(show.name)
                }
        }

        return null
    }

    // endregion
}
