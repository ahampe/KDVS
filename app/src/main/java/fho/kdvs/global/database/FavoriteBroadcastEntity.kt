package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favoriteBroadcastData",
    foreignKeys = [
        ForeignKey(
            entity = BroadcastEntity::class,
            parentColumns = ["broadcastId"],
            childColumns = ["broadcastId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class FavoriteBroadcastEntity (
    @PrimaryKey(autoGenerate = true) val favoriteBroadcastId: Int = 0,
    @ColumnInfo(name = "broadcastId", index = true) val broadcastId: Int = 0
)
