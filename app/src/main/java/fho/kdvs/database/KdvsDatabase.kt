package fho.kdvs.database

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.room.*
import fho.kdvs.database.daos.BroadcastDao
import fho.kdvs.database.daos.ShowDao
import fho.kdvs.database.daos.TrackDao
import fho.kdvs.database.entities.BroadcastEntity
import fho.kdvs.database.entities.ShowEntity
import fho.kdvs.database.entities.TrackEntity
import fho.kdvs.database.models.Day
import fho.kdvs.database.models.Quarter
import java.util.*

@Database(entities = [ShowEntity::class, BroadcastEntity::class, TrackEntity::class], version = 1)
@TypeConverters(
    DateTypeConverter::class,
    DayTypeConverter::class,
    QuarterTypeConverter::class
)
abstract class KdvsDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao
    abstract fun broadcastDao(): BroadcastDao
    abstract fun trackDao(): TrackDao

    companion object {
        private const val FILENAME = "kdvs.db"

        fun initialize(context: Context): KdvsDatabase {
            return Room.databaseBuilder(context.applicationContext, KdvsDatabase::class.java, FILENAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
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