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
    @PrimaryKey(autoGenerate = false) var broadcastId: Int,
    @ColumnInfo(name = "position") var position: Int? = null,
    @ColumnInfo(name = "artist") var artist: String? = null,
    @ColumnInfo(name = "song") var song: String? = null,
    @ColumnInfo(name = "album") var album: String? = null,
    @ColumnInfo(name = "label") var label: String? = null,
    @ColumnInfo(name = "comment") var comment: String? = null,
    @ColumnInfo(name = "airbreak") var airbreak: Boolean = false
)  {
//    constructor() : this(0, 0, "", "", "", "", "", false)
//    constructor(track: Track) : this() {
//        broadcastId = track.broadcastId
//        position = track.position
//        artist = track.artist
//        song = track.song
//        album = track.album
//        label = track.label
//        comment = track.comment
//        airbreak = track.airbreak
//    }
}