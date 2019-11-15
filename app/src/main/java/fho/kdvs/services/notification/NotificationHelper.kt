package fho.kdvs.services.notification

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import fho.kdvs.R
import fho.kdvs.global.extensions.isPlaying

object NotificationHelper {

    fun getPlayOrPauseAction(context: Context, playbackStateCompat: PlaybackStateCompat) =
        if (playbackStateCompat.isPlaying) getPauseAction(
            context
        ) else getPlayAction(context)

    private fun getPlayAction(context: Context) = NotificationCompat.Action(
        R.drawable.exo_controls_play,
        context.getString(R.string.notification_play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )

    private fun getPauseAction(context: Context) = NotificationCompat.Action(
        R.drawable.exo_controls_pause,
        context.getString(R.string.notification_pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
    )

    fun getStopPendingIntent(context: Context) =
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)!!

    fun getStopAction(context: Context) = NotificationCompat.Action(
        R.drawable.exo_icon_stop,
        context.getString(R.string.notification_stop),
        getStopPendingIntent(context)
    )
}