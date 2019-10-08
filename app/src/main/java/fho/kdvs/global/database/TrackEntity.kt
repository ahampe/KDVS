package fho.kdvs.global.database

import android.os.Parcel
import android.os.Parcelable
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
    @PrimaryKey(autoGenerate = true) val trackId: Int = 0,
    @ColumnInfo(name = "broadcastId") val broadcastId: Int = 0,
    @ColumnInfo(name = "position") var position: Int? = null,
    @ColumnInfo(name = "artist") var artist: String? = null,
    @ColumnInfo(name = "song") var song: String? = null,
    @ColumnInfo(name = "album") var album: String? = null,
    @ColumnInfo(name = "label") var label: String? = null,
    @ColumnInfo(name = "comment") var comment: String? = null,
    @ColumnInfo(name = "airbreak") var airbreak: Boolean = false,
    @ColumnInfo(name = "imageHref") var imageHref: String? = null,
    @ColumnInfo(name = "year") var year: Int? = null,
    @ColumnInfo(name = "spotifyAlbumUri") var spotifyAlbumUri: String? = null,
    @ColumnInfo(name = "spotifyTrackUri") var spotifyTrackUri: String? = null,
    @ColumnInfo(name = "youTubeId") var youTubeId: String? = null,
    @ColumnInfo(name = "hasThirdPartyInfo") var hasThirdPartyInfo: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        trackId = parcel.readInt(),
        broadcastId = parcel.readInt(),
        position = parcel.readInt(),
        artist = parcel.readString(),
        song = parcel.readString(),
        album = parcel.readString(),
        label = parcel.readString(),
        comment = parcel.readString(),
        airbreak = parcel.readValue(Boolean::class.java.classLoader) as Boolean,
        imageHref = parcel.readString(),
        year = parcel.readInt(),
        spotifyAlbumUri = parcel.readString(),
        spotifyTrackUri = parcel.readString(),
        youTubeId = parcel.readString(),
        hasThirdPartyInfo = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeValue(trackId)
        dest?.writeValue(broadcastId)
        dest?.writeValue(position)
        dest?.writeValue(artist)
        dest?.writeValue(song)
        dest?.writeValue(album)
        dest?.writeValue(label)
        dest?.writeValue(comment)
        dest?.writeValue(airbreak)
        dest?.writeValue(imageHref)
        dest?.writeValue(year)
        dest?.writeValue(spotifyAlbumUri)
        dest?.writeValue(spotifyTrackUri)
        dest?.writeValue(youTubeId)
        dest?.writeValue(hasThirdPartyInfo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField val CREATOR = object : Parcelable.Creator<TrackEntity> {
            override fun createFromParcel(parcel: Parcel) = TrackEntity(parcel)

            override fun newArray(size: Int) = arrayOfNulls<TrackEntity>(size)
        }
    }
}
