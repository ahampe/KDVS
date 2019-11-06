package fho.kdvs.global.database

import androidx.room.*

@Entity(
    tableName = "favoriteBroadcastData",
    foreignKeys = [
        ForeignKey(
            entity = BroadcastEntity::class,
            parentColumns = ["broadcastId"],
            childColumns = ["broadcastId"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [
        Index(
            value = ["broadcastId"],
            unique = true
        )]
)
data class FavoriteBroadcastEntity(
    @PrimaryKey(autoGenerate = true) val favoriteBroadcastId: Int = 0,
    @ColumnInfo(name = "broadcastId") val broadcastId: Int = 0
)
