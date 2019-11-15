package fho.kdvs.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.HttpDataSource
import dagger.android.AndroidInjection
import fho.kdvs.R
import timber.log.Timber
import javax.inject.Inject

class AudioPlayerService : MediaBrowserServiceCompat() {
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var playbackNotificationBuilder: PlaybackNotificationBuilder
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var builder: NotificationCompat.Builder

    @Inject
    lateinit var playbackPreparer: KdvsPlaybackPreparer

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSessionConnection: MediaSessionConnection

    private var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)

        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.d("Player error: $error")

                if (error?.cause is HttpDataSource.HttpDataSourceException) {
                    Toast.makeText(
                        applicationContext,
                        "Error connecting to KDVS stream. Please check your connection or try again later.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "There was a playback error. Please try again.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        })

        // Build a PendingIntent that can be used to launch the UI.
        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        // Create a new MediaSession.
        mediaSession = MediaSessionCompat(this, "AudioPlayerService")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                isActive = true
            }

        /**
         * In order for [MediaBrowserCompat.ConnectionCallback.onConnected] to be called,
         * a [MediaSessionCompat.Token] needs to be set on the [MediaBrowserServiceCompat].
         *
         * It is possible to wait to set the session token, if required for a specific use-case.
         * However, the token *must* be set by the time [MediaBrowserServiceCompat.onGetRoot]
         * returns, or the connection will fail silently. (The system will not even call
         * [MediaBrowserCompat.ConnectionCallback.onConnectionFailed].)
         */
        sessionToken = mediaSession.sessionToken

        // Override default PlayerNotificationManager to allow for custom actions
        val playerNotificationManager = PlayerNotificationManager(
            this,
            NOW_PLAYING_CHANNEL,
            NOW_PLAYING_NOTIFICATION,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun createCurrentContentIntent(player: Player?): PendingIntent? {
                    return null
                }

                override fun getCurrentContentText(player: Player?): String? {
                    return if (::mediaController.isInitialized)
                        mediaController.metadata?.description?.subtitle.toString()
                    else null
                }

                override fun getCurrentContentTitle(player: Player?): String {
                    return if (::mediaController.isInitialized)
                        mediaController.metadata?.description?.title.toString()
                    else ""
                }

                override fun getCurrentLargeIcon(
                    player: Player?,
                    callback: PlayerNotificationManager.BitmapCallback?
                ): Bitmap? {
                    return if (::mediaController.isInitialized)
                        mediaController.metadata?.description?.iconBitmap
                    else null
                }
            },
            object : PlayerNotificationManager.CustomActionReceiver {
                override fun createCustomActions(
                    context: Context?,
                    instanceId: Int
                ): MutableMap<String, NotificationCompat.Action> {
                    return if (context != null)
                        CustomActionDefinitions(context).getActionMap()
                    else
                        mutableMapOf()
                }

                override fun getCustomActions(player: Player?): MutableList<String> {
                    val playbackType = PlaybackTypeHelper.getPlaybackTypeFromTag(
                        player?.currentTag.toString(), applicationContext
                    )

                    return when (playbackType) {
                        PlaybackType.LIVE -> CustomActionNames.liveActionNames
                        PlaybackType.ARCHIVE -> CustomActionNames.archiveActionNames
                        else -> mutableListOf()
                    }
                }

                override fun onCustomAction(player: Player?, action: String?, intent: Intent?) {
                    Timber.d("Custom action $action performed")
                    val token = sessionToken

                    token?.let {
                        val controller = MediaControllerCompat(baseContext, token)
                        val customAction = CustomAction(
                            application,
                            controller.transportControls,
                            mediaController.playbackState,
                            mediaSessionConnection
                        )

                        when (action) {
                            CustomActionEnum.LIVE.name -> customAction.live()
                            CustomActionEnum.REPLAY.name -> customAction.replay()
                            CustomActionEnum.FORWARD.name -> customAction.forward()
                            else -> null
                        }
                    }
                }
            }
        )

        playerNotificationManager.setNotificationListener(object :
            PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int) {
                stopSelf()
                removeNowPlayingNotification()
                isForegroundService = false
            }

            override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                startForeground(notificationId, notification)
                playerNotificationManager.setUseNavigationActions(false)
            }
        })

        playerNotificationManager.setFastForwardIncrementMs(0)
        playerNotificationManager.setRewindIncrementMs(0)
        playerNotificationManager.setPlayer(exoPlayer)
        playerNotificationManager.setMediaSessionToken(sessionToken)

        // Because ExoPlayer will manage the MediaSession, add the service as a callback for
        // state changes.
        mediaController = MediaControllerCompat(this, mediaSession).also {
            it.registerCallback(MediaControllerCallback(applicationContext))
        }

        notificationManager = NotificationManagerCompat.from(this)

        becomingNoisyReceiver =
            BecomingNoisyReceiver(context = this, sessionToken = mediaSession.sessionToken)

        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession).also {
            it.setPlayer(exoPlayer, playbackPreparer)
            it.setQueueNavigator(KdvsQueueNavigator(mediaSession))
        }

        playbackPreparer.streamMetadataChangedLiveData.observeForever { metadata ->
            mediaSession.setMetadata(metadata)
        }
    }

    /**
     * This is the code that causes KDVS to stop playing when swiping it away from recents.
     * The choice to do this is app specific. Some apps stop playback, while others allow playback
     * to continue and allow uses to stop it with the notification.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        /**
         * By stopping playback, the player will transition to [Player.STATE_IDLE]. This will
         * cause a state change in the MediaSession, and (most importantly) call
         * [MediaControllerCallback.onPlaybackStateChanged]. Because the playback state will
         * be reported as [PlaybackStateCompat.STATE_NONE], the service will first remove
         * itself as a foreground service, and will then call [stopSelf].
         */
        exoPlayer.stop(true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }
    }

    /**
     * Returns the "root" media ID that the client should request to get the list of
     * [MediaItem]s to browse/play.
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("/", null)
    }

    /**
     * Returns (via the [result] parameter) a list of [MediaItem]s that are child
     * items of the provided [parentMediaId]. See [BrowseTree] for more details on
     * how this is build/more details about the relationships.
     */
    override fun onLoadChildren(
        parentMediaId: String,
        result: MediaBrowserServiceCompat.Result<List<MediaItem>>
    ) = result.sendResult(emptyList())


    /**
     * Removes the [NOW_PLAYING_NOTIFICATION] notification.
     *
     * Since `stopForeground(false)` was already called (see
     * [MediaControllerCallback.onPlaybackStateChanged], it's possible to cancel the notification
     * with `notificationManager.cancel(NOW_PLAYING_NOTIFICATION)` if minSdkVersion is >=
     * [Build.VERSION_CODES.LOLLIPOP].
     *
     * Prior to [Build.VERSION_CODES.LOLLIPOP], notifications associated with a foreground
     * service remained marked as "ongoing" even after calling [Service.stopForeground],
     * and cannot be cancelled normally.
     *
     * Fortunately, it's possible to simply call [Service.stopForeground] a second time, this
     * time with `true`. This won't change anything about the service's state, but will simply
     * remove the notification.
     */
    private fun removeNowPlayingNotification() {
        stopForeground(true)
    }

    /**
     * Class to receive callbacks about state changes to the [MediaSessionCompat]. In response
     * to those callbacks, this class:
     *
     * - Build/update the service's notification.
     * - Register/unregister a broadcast receiver for [AudioManager.ACTION_AUDIO_BECOMING_NOISY].
     * - Calls [Service.startForeground] and [Service.stopForeground].
     */
    private inner class MediaControllerCallback(private val context: Context) :
        MediaControllerCompat.Callback() {
        private val platformNotificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /**
         * This is called when initializing a live stream or archive playback.
         */
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaController.playbackState?.let { initNotification(it) }
        }

        /**
         * TODO: prevent notification flicker -- the problem is that if we don't call updateNotification and rebuild the notification,
         * then custom actions don't work and the builder options (e.g. small icon) aren't reflected, for whatever reason
         */
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let { updateNotification(it) }
        }

        private fun initNotification(state: PlaybackStateCompat) {
            if (shouldCreateNowPlayingChannel()) {
                createNowPlayingChannel()
            }

            if (mediaController.metadata == null) {
                removeNowPlayingNotification()
                return
            }

            val playbackType = getPlaybackType()

            val notification = buildNotification(state, playbackType)
            when (state.state) {
                PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.STATE_PLAYING -> {
                    becomingNoisyReceiver.register()

                    /**
                     * This may look strange, but the documentation for [Service.startForeground]
                     * notes that "calling this method does *not* put the service in the started
                     * state itself, even though the name sounds like it."
                     */
                    notification?.let {
                        if (!isForegroundService) {
                            startService(
                                Intent(
                                    applicationContext,
                                    this@AudioPlayerService.javaClass
                                )
                            )
                            startForeground(NOW_PLAYING_NOTIFICATION, it)
                            isForegroundService = true
                        } else {
                            notificationManager.notify(NOW_PLAYING_NOTIFICATION, it)
                        }
                    }
                }
                else -> {
                    becomingNoisyReceiver.unregister()

                    if (isForegroundService) {
                        stopForeground(false)
                        isForegroundService = false

                        // If playback has ended, also stop the service.
                        if (state.state == PlaybackStateCompat.STATE_NONE) {
                            stopSelf()
                        }

                        if (notification != null) {
                            notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                        } else {
                            removeNowPlayingNotification()
                        }
                    }
                }
            }
        }

        private fun updateNotification(state: PlaybackStateCompat) {
            if (::playbackNotificationBuilder.isInitialized) {
                notificationManager.notify(
                    NOW_PLAYING_NOTIFICATION,
                    playbackNotificationBuilder.togglePlay()
                )
            }
        }

        private fun buildNotification(
            state: PlaybackStateCompat,
            playbackType: PlaybackType?
        ): Notification? =
            if (isNotificationBuildRequired(
                    state
                ) && playbackType != null
            ) {
                playbackNotificationBuilder = PlaybackNotificationBuilder(
                    context,
                    mediaSession.sessionToken,
                    playbackType
                )

                playbackNotificationBuilder.build()
            } else null

        /** We're only concerned with playback state changes on play / pause. */
        private fun isNotificationBuildRequired(
            state: PlaybackStateCompat
        ) =
            (state.state == PlaybackState.STATE_PAUSED || state.state == PlaybackState.STATE_PLAYING)

        private fun getPlaybackType() = PlaybackTypeHelper.getPlaybackTypeFromTag(
            mediaController.metadata?.description?.title.toString(), applicationContext
        )

        fun shouldCreateNowPlayingChannel() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

        @RequiresApi(Build.VERSION_CODES.O)
        fun nowPlayingChannelExists() =
            platformNotificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null

        @RequiresApi(Build.VERSION_CODES.O)
        fun createNowPlayingChannel() {
            val notificationChannel = NotificationChannel(
                NOW_PLAYING_CHANNEL,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }

            platformNotificationManager.createNotificationChannel(notificationChannel)
        }
    }
}

/**
 * Helper class to retrieve the the Metadata necessary for the ExoPlayer MediaSession connection
 * extension to call [MediaSessionCompat.setMetadata].
 */
private class KdvsQueueNavigator(
    mediaSession: MediaSessionCompat
) : TimelineQueueNavigator(mediaSession) {
    private val window = Timeline.Window()
    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
        player.currentTimeline
            .getWindow(windowIndex, window, true).tag as MediaDescriptionCompat
}

/**
 * Helper class for listening for when headphones are unplugged (or the audio
 * will otherwise cause playback to become "noisy").
 */
internal class BecomingNoisyReceiver(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token
) : BroadcastReceiver() {

    private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val controller = MediaControllerCompat(context, sessionToken)

    private var registered = false

    fun register() {
        if (!registered) {
            context.registerReceiver(this, noisyIntentFilter)
            registered = true
        }
    }

    fun unregister() {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            controller.transportControls.pause()
        }
    }
}