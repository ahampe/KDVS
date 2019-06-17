package fho.kdvs.global.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fho.kdvs.global.web.MusicBrainzReleaseData
import fho.kdvs.global.web.Spotify
import fho.kdvs.global.web.SpotifyData
import fho.kdvs.topmusic.TopMusicType
import org.threeten.bp.LocalDate

@Entity(tableName = "topMusicData")
data class TopMusicEntity(
    @PrimaryKey(autoGenerate = true) var topMusicId: Int = 0,
    @ColumnInfo(name = "weekOf") var weekOf: LocalDate? = null,
    @ColumnInfo(name = "type") var type: TopMusicType? = null,
    @ColumnInfo(name = "position") var position: Int? = null,
    @ColumnInfo(name = "artist") var artist: String? = null,
    @ColumnInfo(name = "album") var album: String? = null,
    @ColumnInfo(name = "year") var year: Int? = null,
    @ColumnInfo(name = "label") var label: String? = null,
    @ColumnInfo(name = "imageHref") var imageHref: String? = null,
    @ColumnInfo(name = "musicBrainzData") var musicBrainzData: MusicBrainzReleaseData? = null,
    @ColumnInfo(name = "spotifyData") var spotifyData: SpotifyData? = null
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
        musicBrainzData = parcel.readValue(MusicBrainzReleaseData::class.java.classLoader) as MusicBrainzReleaseData,
        spotifyData = parcel.readValue(SpotifyData::class.java.classLoader) as SpotifyData
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
        dest?.writeValue(musicBrainzData)
        dest?.writeValue(spotifyData)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun hasScrapedMetadata(): Boolean {
        return musicBrainzData != null && spotifyData != null
    }

    companion object {
        @JvmField val CREATOR = object : Parcelable.Creator<TrackEntity> {
            override fun createFromParcel(parcel: Parcel) = TrackEntity(parcel)

            override fun newArray(size: Int) = arrayOfNulls<TrackEntity>(size)
        }
    }
}