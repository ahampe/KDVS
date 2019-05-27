/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fho.kdvs.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import fho.kdvs.R
import fho.kdvs.global.extensions.isPlayEnabled
import fho.kdvs.global.extensions.isPlaying

const val NOW_PLAYING_CHANNEL: String = "fho.kdvs.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION: Int = 0xb339

/**
 * Abstract helper class to encapsulate code for building notifications.
 * Concrete classes are instantiated based on [PlaybackType].
 */
abstract class NotificationBuilder(private val context: Context) {
    lateinit var builder: NotificationCompat.Builder

    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val playAction = NotificationCompat.Action(
        R.drawable.exo_controls_play,
        context.getString(R.string.notification_play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_PLAY)
    )
    val pauseAction = NotificationCompat.Action(
        R.drawable.exo_controls_pause,
        context.getString(R.string.notification_pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_PAUSE)
    )

    private val stopPendingIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_STOP)

    abstract fun setBuilder(
        sessionToken: MediaSessionCompat.Token,
        controller: MediaControllerCompat
    )

    fun buildNotification(sessionToken: MediaSessionCompat.Token): Notification {
        val controller = MediaControllerCompat(context, sessionToken)

        setBuilder(sessionToken, controller)

        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel()
        }

        val description = controller.metadata.description
        val playPauseIndex = 1

        val mediaStyle = MediaStyle()
            .setCancelButtonIntent(stopPendingIntent)
            .setMediaSession(sessionToken)
            .setShowActionsInCompactView(playPauseIndex)
            .setShowCancelButton(true)

        // TODO change content text / title, etc.
        return builder.setContentIntent(controller.sessionActivity)
            .setContentText(description.subtitle)
            .setContentTitle(description.title)
            .setDeleteIntent(stopPendingIntent)
            .setLargeIcon(description.iconBitmap)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_radio_white_24dp)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun shouldCreateNowPlayingChannel() =
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
        )
            .apply {
                description = context.getString(R.string.notification_channel_description)
            }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }
}

class LiveNotificationBuilder(val context: Context): NotificationBuilder(context) {
    private val stopAction = NotificationCompat.Action(
        R.drawable.exo_icon_stop,
        context.getString(R.string.notification_stop),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_STOP)
    )
    private val liveAction = NotificationCompat.Action(
        R.drawable.ic_live,
        context.getString(R.string.notification_live),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent("live").setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    override fun setBuilder(
        sessionToken: MediaSessionCompat.Token,
        controller: MediaControllerCompat
    ) {
        val playbackState = controller.playbackState
        builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        builder.addAction(stopAction)

        if (playbackState.isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }

        builder.addAction(liveAction)
    }
}

class ArchiveNotificationBuilder(val context: Context): NotificationBuilder(context) {
    private val replayAction = NotificationCompat.Action(
        R.drawable.ic_replay_30_white_24dp,
        context.getString(R.string.notification_replay),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent("replay").setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )
    private val forwardAction = NotificationCompat.Action(
        R.drawable.ic_forward_30_white_24dp,
        context.getString(R.string.notification_forward),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent("forward").setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    override fun setBuilder(
        sessionToken: MediaSessionCompat.Token,
        controller: MediaControllerCompat
    ) {
        val playbackState = controller.playbackState
        builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        builder.addAction(replayAction)

        if (playbackState.isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }

        builder.addAction(forwardAction)
    }
}
