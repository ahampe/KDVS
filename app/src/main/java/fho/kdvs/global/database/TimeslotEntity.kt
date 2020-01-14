package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime


@Entity(
    tableName = "timeslotData",
    foreignKeys = [
        ForeignKey(
            entity = ShowEntity::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class TimeslotEntity(
    @PrimaryKey(autoGenerate = false) val timeslotId: Int,
    @ColumnInfo(name = "showId", index = true) val showId: Int = 0,
    @ColumnInfo(name = "defaultImageHref") var defaultImageHref: String? = null,
    @ColumnInfo(name = "timeStart") var timeStart: OffsetDateTime? = null,
    @ColumnInfo(name = "timeEnd") var timeEnd: OffsetDateTime? = null
)