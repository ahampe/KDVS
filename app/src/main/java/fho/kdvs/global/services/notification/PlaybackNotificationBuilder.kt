package fho.kdvs.services.notification

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import fho.kdvs.R

const val NOW_PLAYING_CHANNEL: String = "fho.kdvs.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION: Int = 0xb339

class PlaybackNotificationBuilder(
    private val context: Context,
    private val sessionToken: MediaSessionCompat.Token,
    playbackType: PlaybackType
) {
    lateinit var builder: NotificationCompat.Builder

    private val controller = MediaControllerCompat(context, sessionToken)

    private val customNotification = when (playbackType) {
        PlaybackType.LIVE -> LiveNotificationBuilder(
            context,
            controller
        )
        PlaybackType.ARCHIVE -> ArchiveNotificationBuilder(
            context,
            controller
        )
    }

    fun build(): Notification {
        return customNotification.makeCustomBuilder(
            makeBaseNotificationBuilder(sessionToken)
        ).build()
    }

    fun togglePlay(): Notification {
        customNotification.togglePlay()

        return build()
    }

    private fun makeBaseNotificationBuilder(sessionToken: MediaSessionCompat.Token): NotificationCompat.Builder {
        val controller = MediaControllerCompat(context, sessionToken)

        val description = controller.metadata.description

        val mediaStyle = MediaStyle()
            .setCancelButtonIntent(
                NotificationHelper.getStopPendingIntent(
                    context
                )
            )
            .setMediaSession(sessionToken)

        return NotificationCompat.Builder(
            context,
            NOW_PLAYING_CHANNEL
        )
            .setContentIntent(controller.sessionActivity)
            .setContentText(description.subtitle ?: "")
            .setContentTitle(description.title ?: "")
            .setDeleteIntent(
                NotificationHelper.getStopPendingIntent(
                    context
                )
            )
            .setLargeIcon(description.iconBitmap)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_kdvs_head_black)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(0)
            .setShowWhen(false)
    }
}


private class LiveNotificationBuilder(
    val mContext: Context,
    val controller: MediaControllerCompat
) :
    CustomNotificationBuilder {

    override val playPause: NotificationCompat.Action
        get() = NotificationHelper.getPlayOrPauseAction(
            context,
            controller.playbackState
        )

    override val context: Context
        get() = mContext

    override fun makeCustomBuilder(
        baseBuilder: NotificationCompat.Builder
    ): NotificationCompat.Builder {
        val customActionDefinitions =
            CustomActionDefinitions(context)

        return baseBuilder
            .addAction(playPause)
            .addAction(customActionDefinitions.liveAction)
            .addAction(NotificationHelper.getStopAction(context))
    }
}

private class ArchiveNotificationBuilder(
    val mContext: Context,
    val controller: MediaControllerCompat
) :
    CustomNotificationBuilder {

    override val playPause: NotificationCompat.Action
        get() = NotificationHelper.getPlayOrPauseAction(
            context,
            controller.playbackState
        )

    override val context: Context
        get() = mContext

    override fun makeCustomBuilder(
        baseBuilder: NotificationCompat.Builder
    ): NotificationCompat.Builder {
        val customActionDefinitions =
            CustomActionDefinitions(context)

        return baseBuilder
            .addAction(playPause)
            .addAction(customActionDefinitions.replayAction)
            .addAction(customActionDefinitions.forwardAction)
            .addAction(NotificationHelper.getStopAction(context))
    }
}
