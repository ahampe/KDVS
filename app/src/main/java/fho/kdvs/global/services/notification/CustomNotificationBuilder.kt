package fho.kdvs.services.notification

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import fho.kdvs.R

interface CustomNotificationBuilder {
    val context: Context
    val playPause: NotificationCompat.Action

    fun makeCustomBuilder(
        baseBuilder: NotificationCompat.Builder
    ): NotificationCompat.Builder

    fun togglePlay() {
        if (playPause.title == context.getString(R.string.notification_play)) {
            playPause.icon = R.drawable.exo_controls_pause
            playPause.title = context.getString(R.string.notification_pause)
            playPause.actionIntent =
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PAUSE
                )
        } else {
            playPause.icon = R.drawable.exo_controls_play
            playPause.title = context.getString(R.string.notification_play)
            playPause.actionIntent =
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PLAY
                )
        }
    }
}