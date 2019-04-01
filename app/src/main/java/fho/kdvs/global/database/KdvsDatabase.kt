package fho.kdvs.global.database

import android.app.Application
import android.content.Context
import androidx.room.*
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.util.TimeHelper
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import java.io.File

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
    abstract fun contactDao(): ContactDao
    abstract fun topMusicDao(): TopMusicDao

    companion object {
        private const val FILE_NAME = "kdvs.db"

        /**
         * Builds the database for development purposes. It falls back to destructive migration so that we skip migrations.
         * In a production setting, we would need to actually write the migrations and not use `fallbackToDestructiveMigration()`
         */
        fun buildDevelopmentDatabase(application: Application) =
            Room.databaseBuilder(application, KdvsDatabase::class.java, KdvsDatabase.FILE_NAME)
                .fallbackToDestructiveMigration()
                .build()

        // TODO buildProductionDatabase

        /**
         * Deletes the database file along with related temporary files.
         *
         * [See the sql documentation for more info on shm and wal](https://www.sqlite.org/tempfiles.html)
         */
        fun deleteDatabaseFile(context: Context) {
            val databases = File(context.applicationInfo.dataDir + "/databases")
            File(databases, "$FILE_NAME-shm").delete()
            File(databases, "$FILE_NAME-wal").delete()
            File(databases, FILE_NAME).delete()
        }
    }
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