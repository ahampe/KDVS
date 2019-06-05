package fho.kdvs.services

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.preferences.KdvsPreferences
import java.util.*
import javax.inject.Inject

class KdvsAlarmManager @Inject constructor(val application: Application) {
    private val context = application.applicationContext
    private val kdvsPreferences = KdvsPreferences(application)

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    fun registerShowAlarm(show: ShowEntity) {
        val timeStart = show.timeStart

        timeStart?.let {
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, show.id, intent, 0)
            }

            val alarmTime = timeStart.minusMinutes(kdvsPreferences.alarmNoticeInterval ?: 0)

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
            }

            cancelShowAlarm(show) // prevent multiple registrations

            alarmMgr?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
        }

    }

    fun cancelShowAlarm(show: ShowEntity) {
        if (alarmMgr == null)
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!::alarmIntent.isInitialized) {
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, show.id, intent, 0)
            }
        }

        alarmMgr?.cancel(alarmIntent)
    }
}
