package fho.kdvs.services

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.services.LiveShowUpdater.Companion.WEEK_IN_MILLIS
import fho.kdvs.show.ShowRepository
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

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

    suspend fun registerShowAlarm(show: ShowEntity) = coroutineScope{
        val timeStart = show.timeStart

        timeStart?.let {
            initShowAlarm(show)

            val showsAtTime = withContext(coroutineContext){
                showRepository.allShowsAtTimeOrderedRelativeToCurrentWeek(timeStart)
            }

            if (showsAtTime.isNotEmpty()){
                val weekOffset = showsAtTime
                    .indexOfFirst{ s -> s?.id == show.id }
                    .toLong()

                val alarmTime = timeStart
                    .plusWeeks(weekOffset)
                    .minusMinutes(kdvsPreferences.alarmNoticeInterval ?: 0)

                val calendar: Calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.DAY_OF_WEEK, alarmTime.dayOfWeek.value)
                    set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                    set(Calendar.MINUTE, alarmTime.minute)
                }

                cancelShowAlarm(show) // prevent multiple registrations

                alarmMgr?.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    WEEK_IN_MILLIS * showsAtTime.size,
                    alarmIntent
                )
            }
        }
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
                intent.putExtra("interval", kdvsPreferences.alarmNoticeInterval?.toInt())
                PendingIntent.getBroadcast(context, show.id, intent, 0)
            }
        }
    }
}
