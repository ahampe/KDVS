package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscriptionData",
    foreignKeys = [
        ForeignKey(
            entity = ShowEntity::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class SubscriptionEntity (
    @PrimaryKey(autoGenerate = true) val subscriptionId: Int = 0,
    @ColumnInfo(name = "showId") val showId: Int = 0
)