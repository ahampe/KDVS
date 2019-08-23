package fho.kdvs.global.database

import android.app.Application
import android.content.Context
import androidx.room.*
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.topmusic.TopMusicType
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import java.io.File


@Database(
    entities = [ShowEntity::class, BroadcastEntity::class, FavoriteBroadcastEntity::class,
        FavoriteTrackEntity::class, TrackEntity::class, StaffEntity::class, NewsEntity::class,
        TopMusicEntity::class, FundraiserEntity::class, SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    OffsetDateTimeTypeConverter::class,
    LocalDateTypeConverter::class,
    QuarterTypeConverter::class,
    TopMusicTypeConverter::class
)
abstract class KdvsDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao
    abstract fun broadcastDao(): BroadcastDao
    abstract fun favoriteBroadcastDao(): FavoriteBroadcastDao
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun trackDao(): TrackDao
    abstract fun topMusicDao(): TopMusicDao
    abstract fun staffDao(): StaffDao
    abstract fun newsDao(): NewsDao
    abstract fun fundraiserDao(): FundraiserDao
    abstract fun subscriptionDao(): SubscriptionDao

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
         * [See the sql JSONObjectation for more info on shm and wal](https://www.sqlite.org/tempfiles.html)
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

class ListOffsetDateTimeTypeConverter {

    @TypeConverter
    fun toOffsetDateTimeList(value: String?): List<OffsetDateTime>? =
        value?.let { it.split(",")
            .map { d -> d.toLong() }
            .map { l -> Instant.ofEpochSecond(l).atOffset(ZoneOffset.UTC) }}

    @TypeConverter
    fun toString(dates: List<OffsetDateTime>?): String? = dates?.joinToString{ d -> d.toString() }
}

/** Type converter for broadcast times. Uses strings internally. */
class LocalDateTypeConverter {

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, TimeHelper.dateFormatter) }

    @TypeConverter
    fun toString(date: LocalDate?): String? = TimeHelper.dateFormatter.format(date)
}

class QuarterTypeConverter {

    @TypeConverter
    fun toInt(value: Quarter?): Int? = value?.ordinal

    @TypeConverter
    fun toQuarter(value: Int?): Quarter? = value?.let { Quarter.values()[value] }
}

class TopMusicTypeConverter {

    @TypeConverter
    fun toInt(value: TopMusicType?): Int? = value?.ordinal

    @TypeConverter
    fun toTopMusicType(value: Int?): TopMusicType? = value?.let { TopMusicType.values()[value] }
}
