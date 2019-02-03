package fho.kdvs.model.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trackData",
    foreignKeys = [
        ForeignKey(
            entity = BroadcastEntity::class,
            parentColumns = ["broadcastId"],
            childColumns = ["broadcastId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val trackId: Int,
    @ColumnInfo(name = "broadcastId") var broadcastId: Int,
    @ColumnInfo(name = "position") var position: Int? = null,
    @ColumnInfo(name = "artist") var artist: String? = null,
    @ColumnInfo(name = "song") var song: String? = null,
    @ColumnInfo(name = "album") var album: String? = null,
    @ColumnInfo(name = "label") var label: String? = null,
    @ColumnInfo(name = "comment") var comment: String? = null,
    @ColumnInfo(name = "airbreak") var airbreak: Boolean = false
)