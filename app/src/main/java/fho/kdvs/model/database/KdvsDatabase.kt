package fho.kdvs.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.daos.BroadcastDao
import fho.kdvs.model.database.daos.ShowDao
import fho.kdvs.model.database.daos.TrackDao
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
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