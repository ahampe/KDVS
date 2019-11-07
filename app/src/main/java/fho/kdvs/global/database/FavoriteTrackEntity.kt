package fho.kdvs.global.database

import androidx.room.*

@Entity(
    tableName = "favoriteTrackData",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["trackId"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [
        Index(
            value = ["trackId"],
            unique = true
        )]
)
data class FavoriteTrackEntity (
    @PrimaryKey(autoGenerate = true) val favoriteTrackId: Int = 0,
    @ColumnInfo(name = "trackId") val trackId: Int = 0
)
