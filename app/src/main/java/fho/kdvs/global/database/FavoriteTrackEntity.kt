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
<<<<<<< HEAD
    @PrimaryKey(autoGenerate = true) val favoriteId: Int = 0,
    @ColumnInfo(name = "trackId", index = true) val trackId: Int = 0
=======
    @PrimaryKey(autoGenerate = true) val favoriteTrackId: Int = 0,
    @ColumnInfo(name = "trackId") val trackId: Int = 0
>>>>>>> further groundwork
)
