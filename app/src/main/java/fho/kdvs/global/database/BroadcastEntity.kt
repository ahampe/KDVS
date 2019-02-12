package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(
    tableName = "broadcastData",
    foreignKeys = [
        ForeignKey(
            entity = ShowEntity::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class BroadcastEntity(
    @PrimaryKey(autoGenerate = false) val broadcastId: Int,
    @ColumnInfo(name = "showId") var showId: Int? = 0,
    @ColumnInfo(name = "descr") var descr: String? = null,
    @ColumnInfo(name = "date") var date: LocalDate? = null,
    @ColumnInfo(name = "imageHref") var imageHref: String? = null
)