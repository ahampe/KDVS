package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favoriteData",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["trackId"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class FavoriteEntity (
    @PrimaryKey(autoGenerate = true) val favoriteId: Int = 0,
    @ColumnInfo(name = "trackId") val trackId: Int = 0
)
