package fho.kdvs.services

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.services.LiveShowUpdater.Companion.WEEK_IN_MILLIS
import fho.kdvs.show.ShowRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/** Class for managing alerts of subscribed shows' broadcasts. */
class KdvsAlarmManager @Inject constructor(
    val application: Application,
    val showRepository: ShowRepository
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.IO

    private val context = application.applicationContext
    private val kdvsPreferences = KdvsPreferences(application)

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    fun registerShowAlarmAsync(show: ShowEntity): Deferred<Boolean> = async{
        val timeStart = show.timeStart

        timeStart?.let {
            initShowAlarm(show)

            val showsAtTime = showRepository.allShowsAtTimeOrderedRelativeToCurrentWeek(timeStart)

            if (showsAtTime.isNotEmpty()){
                val weekOffset = showsAtTime
                    .indexOfFirst{ s -> s?.id == show.id }
                    .toLong()

                val adjustedTime = TimeHelper.makeRealWeekRelativeTimeFromEpochTime(timeStart)

                val alarmTime = adjustedTime
                    ?.plusWeeks(weekOffset)
                    ?.minusMinutes(kdvsPreferences.alarmNoticeInterval ?: 10)

                alarmTime?.let {
                    cancelShowAlarm(show) // prevent multiple registrations

                    alarmMgr?.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.toEpochSecond() * 1000,
                        WEEK_IN_MILLIS * showsAtTime.size,
                        alarmIntent
                    )

                    return@async true
                }
            }
        }

        return@async false
    }

    fun cancelShowAlarm(show: ShowEntity) {
        initShowAlarm(show)
        alarmMgr?.cancel(alarmIntent)
    }

    private fun initShowAlarm(show: ShowEntity) {
        if (alarmMgr == null)
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!::alarmIntent.isInitialized) {
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.putExtra("showName", show.name)
                intent.putExtra("interval", kdvsPreferences.alarmNoticeInterval?.toInt() ?: 10)
                PendingIntent.getBroadcast(context, show.id, intent, 0)
            }
        }
    }
}
