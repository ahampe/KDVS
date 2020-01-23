package fho.kdvs.global.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fho.kdvs.topmusic.TopMusicType
import org.threeten.bp.LocalDate

@Entity(tableName = "topMusicData")
data class TopMusicEntity(
    @PrimaryKey(autoGenerate = true) val topMusicId: Int = 0,
    @ColumnInfo(name = "weekOf") var weekOf: LocalDate? = null,
    @ColumnInfo(name = "type") var type: TopMusicType? = null,
    @ColumnInfo(name = "position") var position: Int? = null,
    @ColumnInfo(name = "artist") var artist: String? = null,
    @ColumnInfo(name = "album") var album: String? = null,
    @ColumnInfo(name = "year") var year: Int? = null,
    @ColumnInfo(name = "label") var label: String? = null,
    @ColumnInfo(name = "imageHref") var imageHref: String? = null,
    @ColumnInfo(name = "spotifyAlbumUri") var spotifyAlbumUri: String? = null,
    @ColumnInfo(name = "spotifyTrackUris") var spotifyTrackUris: String? = null, // TODO make this a one-to-many join on separate table?
    @ColumnInfo(name = "youTubeId") var youTubeId: String? = null,
    @ColumnInfo(name = "hasThirdPartyInfo") var hasThirdPartyInfo: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        topMusicId = parcel.readInt(),
        weekOf = parcel.readValue(LocalDate::class.java.classLoader) as LocalDate,
        type = parcel.readValue(TopMusicType::class.java.classLoader) as TopMusicType,
        position = parcel.readInt(),
        artist = parcel.readString(),
        album = parcel.readString(),
        year = parcel.readInt(),
        label = parcel.readString(),
        imageHref = parcel.readString(),
        spotifyAlbumUri = parcel.readString(),
        spotifyTrackUris = parcel.readString(),
        youTubeId = parcel.readString(),
        hasThirdPartyInfo = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeValue(topMusicId)
        dest?.writeValue(weekOf)
        dest?.writeValue(type)
        dest?.writeValue(position)
        dest?.writeValue(artist)
        dest?.writeValue(album)
        dest?.writeValue(year)
        dest?.writeValue(label)
        dest?.writeValue(imageHref)
        dest?.writeValue(spotifyAlbumUri)
        dest?.writeValue(spotifyTrackUris)
        dest?.writeValue(youTubeId)
        dest?.writeValue(hasThirdPartyInfo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<TrackEntity> {
            override fun createFromParcel(parcel: Parcel) = TrackEntity(parcel)

            override fun newArray(size: Int) = arrayOfNulls<TrackEntity>(size)
        }
    }
}
