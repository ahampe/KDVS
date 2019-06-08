package fho.kdvs.global.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "topMusicData")
data class TopMusicEntity(
    @PrimaryKey(autoGenerate = true) var topMusicId: Int = 0,
    @ColumnInfo(name = "weekOf") var weekOf: LocalDate? = null,
    @ColumnInfo(name = "position") var position: Int? = null,
    @ColumnInfo(name = "artist") var artist: String? = null,
    @ColumnInfo(name = "album") var album: String? = null,
    @ColumnInfo(name = "label") var label: String? = null,
    @ColumnInfo(name = "isNewAdd") var isNewAdd: Boolean = false,
    @ColumnInfo(name = "imageHref") var imageHref: String? = null,
    @ColumnInfo(name = "year") var year: Int? = null,
    @ColumnInfo(name = "hasScrapedMetadata") var hasScrapedMetadata: Boolean = false,
    @ColumnInfo(name = "spotifyUri") var spotifyUri: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        topMusicId = parcel.readInt(),
        weekOf = parcel.readValue(LocalDate::class.java.classLoader) as LocalDate,
        position = parcel.readInt(),
        artist = parcel.readString(),
        album = parcel.readString(),
        label = parcel.readString(),
        isNewAdd = parcel.readValue(Boolean::class.java.classLoader) as Boolean,
        imageHref = parcel.readString(),
        year = parcel.readInt(),
        hasScrapedMetadata = parcel.readValue(Boolean::class.java.classLoader) as Boolean,
        spotifyUri = parcel.readString()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeValue(topMusicId)
        dest?.writeValue(weekOf)
        dest?.writeValue(position)
        dest?.writeValue(artist)
        dest?.writeValue(album)
        dest?.writeValue(label)
        dest?.writeValue(isNewAdd)
        dest?.writeValue(imageHref)
        dest?.writeValue(year)
        dest?.writeValue(hasScrapedMetadata)
        dest?.writeValue(spotifyUri)
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