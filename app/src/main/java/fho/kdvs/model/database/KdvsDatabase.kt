package fho.kdvs.model.database

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.room.*
import fho.kdvs.model.database.daos.BroadcastDao
import fho.kdvs.model.database.daos.ShowDao
import fho.kdvs.model.database.daos.TrackDao
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
import fho.kdvs.model.database.models.Day
import fho.kdvs.model.database.models.Quarter
import java.util.*

@Database(
    entities = [ShowEntity::class, BroadcastEntity::class, TrackEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DateTypeConverter::class,
    DayTypeConverter::class,
    QuarterTypeConverter::class
)
abstract class KdvsDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao
    abstract fun broadcastDao(): BroadcastDao
    abstract fun trackDao(): TrackDao
}

class DbWorkerThread(threadName: String) : HandlerThread(threadName) {

    private var mWorkerHandler: Handler? = null

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mWorkerHandler = Handler(looper)
    }

    fun postTask(task: Runnable) {
        mWorkerHandler?.post(task)
    }

}

class DateTypeConverter {

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun toLong(value: Date?): Long? = value?.time
}

class DayTypeConverter {

    @TypeConverter
    fun toString(value: Day?): String? = value.toString()

    @TypeConverter
    fun toDay(value: String?): Day? = value?.let { Day.valueOf(it) }
}

class QuarterTypeConverter {

    @TypeConverter
    fun toString(value: Quarter?): String? = value.toString()

    @TypeConverter
    fun toQuarter(value: String?): Quarter? = value?.let { Quarter.valueOf(it) }
}