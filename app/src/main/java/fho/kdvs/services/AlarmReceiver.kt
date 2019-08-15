package fho.kdvs.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import fho.kdvs.R
import fho.kdvs.global.MainActivity
import timber.log.Timber


const val ALARM_CHANNEL: String = "fho.kdvs.ALARM"
const val ALARM_NOTIFICATION: Int = 0xb340

/**
 * Class to receive alarms created by [KdvsAlarmManager], for e.g. subscribed shows' live-broadcast notices.
 */
class AlarmReceiver: BroadcastReceiver() {
    private lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "android.intent.action.BOOT_COMPLETED" -> {
                // TODO: reset alarm upon reboot
            }
            else -> {
                context?.let {
                    val title = context.resources.getString(R.string.show_notification_title)
                    val name = intent?.extras?.getString("showName")
                    val interval = intent?.extras?.getInt("interval")
                    val text = context.resources.getString(
                        R.string.show_notification_text_live,
                        name,
                        if (interval == 0)
                            context.resources.getString(R.string.show_notification_text_now)
                        else
                            context.resources.getString(R.string.show_notification_text_interval, interval)
                    )

                    Timber.d("Alarm received for $name in $interval minutes.")

                    // TODO: change to splash activity

                    val contentIntent = PendingIntent.getActivity(
                        context, 0,
                        Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val notification = NotificationCompat.Builder(context, ALARM_CHANNEL)
                        .setSmallIcon(R.drawable.ic_kdvs_head_white)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(contentIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setVibrate(longArrayOf(0, 400, 200, 400))
                        .setLights(Color.WHITE, 1000, 1000)
                        .build()

                    notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if (shouldCreateAlarmChannel()) {
                        createAlarmChannel(context)
                    }

                    notificationManager.notify(ALARM_NOTIFICATION, notification)
                }
            }
        }
    }

    private fun shouldCreateAlarmChannel() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !alarmChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    fun alarmChannelExists() =
        notificationManager.getNotificationChannel(ALARM_CHANNEL) != null

    @RequiresApi(Build.VERSION_CODES.O)
    fun createAlarmChannel(context: Context) {
        val notificationChannel = NotificationChannel(
            ALARM_CHANNEL,
            context.getString(R.string.alarm_channel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.alarm_channel_description)
            enableLights(true)
            enableVibration(true)
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }
}