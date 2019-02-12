package fho.kdvs.global.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.util.TimeHelper
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

@Database(
    entities = [ShowEntity::class, BroadcastEntity::class, TrackEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    OffsetDateTimeTypeConverter::class,
    LocalDateTypeConverter::class,
    QuarterTypeConverter::class
)
abstract class KdvsDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao
    abstract fun broadcastDao(): BroadcastDao
    abstract fun trackDao(): TrackDao
}

/** Type converter for show times. Uses Epoch seconds internally. */
class OffsetDateTimeTypeConverter {

    @TypeConverter
    fun toOffsetDateTime(value: Long?): OffsetDateTime? =
        value?.let { Instant.ofEpochSecond(it).atOffset(ZoneOffset.UTC) }

    @TypeConverter
    fun toLong(date: OffsetDateTime?): Long? = date?.toEpochSecond()
}

/** Type converter for broadcast times. Uses strings internally. */
class LocalDateTypeConverter {

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, TimeHelper.dateFormatter) }

    @TypeConverter
    fun toString(date: LocalDate?): String? = TimeHelper.dateFormatter.format(date)
}

class QuarterTypeConverter {

    @TypeConverter
    fun toInt(value: Quarter?): Int? = value?.ordinal

    @TypeConverter
    fun toQuarter(value: Int?): Quarter? = value?.let { Quarter.values()[value] }
}