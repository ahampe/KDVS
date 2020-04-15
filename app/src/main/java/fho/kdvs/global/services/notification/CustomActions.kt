package fho.kdvs.services.notification

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import fho.kdvs.R
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.extensions.isPrepared
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.services.MediaSessionConnection
import javax.inject.Inject
import kotlin.math.max

enum class CustomActionEnum(val type: String) {
    LIVE("live"),
    REPLAY("replay"),
    FORWARD("forward")
}

object CustomActionNames {
    val liveActionNames = mutableListOf(
        CustomActionEnum.LIVE.name
    )

    val archiveActionNames = mutableListOf(
        CustomActionEnum.REPLAY.name,
        CustomActionEnum.FORWARD.name
    )
}

class CustomActionDefinitions(val context: Context) {
    val liveAction = NotificationCompat.Action(
        R.drawable.ic_live_white_24dp,
        context.getString(R.string.notification_live),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(CustomActionEnum.LIVE.name).setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    val replayAction = NotificationCompat.Action(
        R.drawable.ic_replay_30_white_24dp,
        context.getString(R.string.notification_replay),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(CustomActionEnum.REPLAY.name).setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    val forwardAction = NotificationCompat.Action(
        R.drawable.ic_forward_30_white_24dp,
        context.getString(R.string.notification_forward),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(CustomActionEnum.FORWARD.name).setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    fun getActionMap(): HashMap<String, NotificationCompat.Action> {
        val hashMap = HashMap<String, NotificationCompat.Action>()

        hashMap[CustomActionEnum.LIVE.name] = liveAction
        hashMap[CustomActionEnum.REPLAY.name] = replayAction
        hashMap[CustomActionEnum.FORWARD.name] = forwardAction

        return hashMap
    }
}

class CustomAction @Inject constructor(
    private val application: Application,
    private val transportControls: MediaControllerCompat.TransportControls?,
    private val playbackState: PlaybackStateCompat?
) {

    fun live() {
        val preferences = KdvsPreferences(application)

        if (preferences.offlineMode == true)
            return

        val streamUrl = preferences.streamUrl ?: URLs.LIVE_OGG
        val isPrepared = playbackState?.isPrepared ?: false

        transportControls?.let {
            if (isPrepared) {
                playbackState?.let {
                    if (it.isPlaying)
                        transportControls.stop()
                }
            }

            transportControls.playFromMediaId(streamUrl, null)
        }
    }

    fun replay() {
        playbackState?.let {
            val currentPos = it.position
            val newPos = max(0, currentPos - 30000)
            transportControls?.seekTo(newPos)
        }
    }

    fun forward() {
        playbackState?.let {
            val currentPos = it.position
            val newPos = currentPos + 30000
            transportControls?.seekTo(newPos)
        }
    }
}

