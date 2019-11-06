package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favoriteTrackData",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["trackId"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class FavoriteTrackEntity (
    @PrimaryKey(autoGenerate = true) val favoriteTrackId: Int = 0,
    @ColumnInfo(name = "trackId", index = true) val trackId: Int = 0
)
