package fho.kdvs.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.daos.BroadcastDao
import fho.kdvs.model.database.daos.ShowDao
import fho.kdvs.model.database.daos.TrackDao
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
import fho.kdvs.util.TimeHelper
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