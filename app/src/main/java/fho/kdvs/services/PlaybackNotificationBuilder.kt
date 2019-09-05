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

import android.app.NotificationManager
import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import fho.kdvs.R
import fho.kdvs.global.extensions.isPlayEnabled
import fho.kdvs.global.extensions.isPlaying

const val NOW_PLAYING_CHANNEL: String = "fho.kdvs.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION: Int = 0xb339

/**
 * Abstract helper class to encapsulate code for building playback notifications.
 * Concrete classes are instantiated based on [PlaybackType].
 */
abstract class PlaybackNotificationBuilder(private val context: Context) {
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

    val stopPendingIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_STOP)

    abstract fun setBuilder(
        sessionToken: MediaSessionCompat.Token,
        controller: MediaControllerCompat
    )

    fun buildNotification(sessionToken: MediaSessionCompat.Token): NotificationCompat.Builder {
        val controller = MediaControllerCompat(context, sessionToken)

        setBuilder(sessionToken, controller)

        val description = controller.metadata.description

        val mediaStyle = MediaStyle()
            .setCancelButtonIntent(stopPendingIntent)
            .setMediaSession(sessionToken)

        builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)
            .setContentIntent(controller.sessionActivity)
            .setContentText(description.subtitle)
            .setContentTitle(description.title)
            .setDeleteIntent(stopPendingIntent)
            .setLargeIcon(description.iconBitmap)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_kdvs_head_black)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)

        return builder
    }
}

class LivePlaybackNotificationBuilder(val context: Context): PlaybackNotificationBuilder(context) {
    override fun setBuilder(sessionToken: MediaSessionCompat.Token, controller: MediaControllerCompat) {
        val customActions = CustomActionDefinitions(context)
        val playbackState = controller.playbackState
        builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        if (playbackState.isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }

        builder.addAction(customActions.liveAction)

        builder.addAction(NotificationCompat.Action(
            R.drawable.exo_icon_stop,
            context.getString(R.string.notification_stop),
            stopPendingIntent
        ))
    }
}

class ArchivePlaybackNotificationBuilder(val context: Context): PlaybackNotificationBuilder(context) {
    override fun setBuilder(sessionToken: MediaSessionCompat.Token, controller: MediaControllerCompat) {
        val customActions = CustomActionDefinitions(context)
        val playbackState = controller.playbackState
        builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        builder.addAction(customActions.replayAction)

        if (playbackState.isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }

        builder.addAction(customActions.forwardAction)
    }
}

class DefaultPlaybackNotificationBuilder(val context: Context): PlaybackNotificationBuilder(context) {
    override fun setBuilder(sessionToken: MediaSessionCompat.Token, controller: MediaControllerCompat) {
        val playbackState = controller.playbackState
        builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        if (playbackState.isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }
    }
}
