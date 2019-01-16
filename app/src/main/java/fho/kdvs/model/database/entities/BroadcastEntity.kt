package fho.kdvs.model.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

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
    @ColumnInfo(name = "desc") var desc: String? = null,
    @ColumnInfo(name = "date") var date: Date? = null,
    @ColumnInfo(name = "imageHref") var imageHref: String? = null
) {
//    constructor() : this(0, 0, "", "", "", null, "")
//    constructor(broadcast: Broadcast) : this() {
//        broadcastId = broadcast.broadcastId
//        showId = broadcast.showId
//        host = broadcast.host
//        genre = broadcast.genre
//        desc = broadcast.desc
//        date = broadcast.date
//        imageHref = broadcast.imageHref
//    }
}