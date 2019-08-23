package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fho.kdvs.global.enums.Quarter
import org.threeten.bp.OffsetDateTime

/**
 * Note: A show here is defined as: a program specific to a given [Quarter] and [Year], unique on
 * the basis of its name, that may occur in multiple [TimeSlot]s a week in the case of syndicated programs
 * (e.g. Democracy Now) or once every n weeks in a given [TimeSlot] in the case of alternating programs.
 */
@Entity(tableName = "showData")
data class ShowEntity(
    @PrimaryKey(autoGenerate = false) val id: Int,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "host") var host: String? = null,
    @ColumnInfo(name = "genre") var genre: String? = null,
    @ColumnInfo(name = "defaultDesc") var defaultDesc: String? = null,
    @ColumnInfo(name = "defaultImageHref") var defaultImageHref: String? = null,
    @ColumnInfo(name = "timeStart") var timeStart: List<OffsetDateTime?>? = null,
    @ColumnInfo(name = "timeEnd") var timeEnd: List<OffsetDateTime?>? = null,
    @ColumnInfo(name = "quarter") var quarter: Quarter? = null,
    @ColumnInfo(name = "year") var year: Int? = null
)