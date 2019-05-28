package fho.kdvs.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import fho.kdvs.R

object CustomActionNames {
    val actionNames = mutableListOf("live", "replay", "forward")
}

class CustomActions(val context: Context) {
    val liveAction = NotificationCompat.Action(
        R.drawable.ic_live,
        context.getString(R.string.notification_live),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent("live").setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    val replayAction = NotificationCompat.Action(
        R.drawable.ic_replay_30_white_24dp,
        context.getString(R.string.notification_replay),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent("replay").setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )
    val forwardAction = NotificationCompat.Action(
        R.drawable.ic_forward_30_white_24dp,
        context.getString(R.string.notification_forward),
        PendingIntent.getBroadcast(
            context,
            0,
            Intent("forward").setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    fun getActionMap(): HashMap<String, NotificationCompat.Action> {
        val hashMap = HashMap<String, NotificationCompat.Action>()

        hashMap["live"] = liveAction
        hashMap["replay"] = replayAction
        hashMap["forward"] = forwardAction

        return hashMap
    }
}