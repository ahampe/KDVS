package fho.kdvs.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fho.kdvs.R
import timber.log.Timber

const val ALARM_CHANNEL: String = "fho.kdvs.ALARM"

/**
 * Class to receive alarms created by [KdvsAlarmManager], for e.g. subscribed shows' live-broadcast notices.
 */
class AlarmReceiver: BroadcastReceiver() {
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val name = intent?.extras?.getString("showName")
            val interval = intent?.extras?.getInt("interval")
            val text = context.resources.getString(
                R.string.show_notification_title,
                if (interval == 0)
                    context.resources.getString(R.string.show_notification_title_now)
                else
                    context.resources.getString(R.string.show_notification_title_interval, interval)
            )

            Timber.d("Alarm received for $name in $interval minutes.")

            val notification = NotificationCompat.Builder(context, ALARM_CHANNEL)
                .setSmallIcon(R.drawable.ic_radio_white_24dp)
                .setContentTitle(name)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
        }
    }
}