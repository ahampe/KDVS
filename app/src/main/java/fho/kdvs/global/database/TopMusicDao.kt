package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fho.kdvs.topmusic.TopMusicType
import org.threeten.bp.LocalDate

@Dao
abstract class TopMusicDao {
    @Query("SELECT * from topMusicData")
    abstract fun getAll(): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData")
    abstract fun getAllTopMusic(): List<TopMusicEntity>

    @Query("SELECT * from topMusicData where type = :type")
    abstract fun getAllOfType(type: TopMusicType): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where type = :type order by weekOf desc limit :limit")
    abstract fun getMostRecentTopMusicForType(
        type: TopMusicType,
        limit: Int
    ): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where type = :type and weekOf = :weekOf")
    abstract fun getTopMusicForWeekOfType(
        weekOf: LocalDate?,
        type: TopMusicType
    ): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where type = :type and weekOf = :weekOf")
    abstract fun topMusicForWeekOfType(
        weekOf: LocalDate?,
        type: TopMusicType
    ): List<TopMusicEntity>

    @Query("UPDATE topMusicData SET spotifyAlbumUri = :spotifyAlbumUri where topMusicId = :id")
    abstract fun updateTopMusicSpotifyAlbumUri(id: Int, spotifyAlbumUri: String)

    @Query("UPDATE topMusicData SET spotifyTrackUris = :spotifyTrackUris where topMusicId = :id")
    abstract fun updateTopMusicSpotifyTrackUris(id: Int, spotifyTrackUris: String)

    @Query("UPDATE topMusicData SET youTubeId = :youTubeId where topMusicId = :id")
    abstract fun updateTopMusicYouTubeId(id: Int, youTubeId: String)

    @Query("UPDATE topMusicData SET hasThirdPartyInfo = :hasThirdPartyInfo where topMusicId = :id")
    abstract fun updateTopMusicHasThirdPartyInfo(id: Int, hasThirdPartyInfo: Boolean)

    @Query("UPDATE topMusicData SET album = :album where topMusicId = :id")
    abstract fun updateTopMusicAlbum(id: Int, album: String)

    @Query("UPDATE topMusicData SET imageHref = :imageHref where topMusicId = :id")
    abstract fun updateTopMusicImageHref(id: Int, imageHref: String)

    @Query("UPDATE topMusicData SET label = :label where topMusicId = :id")
    abstract fun updateTopMusicLabel(id: Int, label: String)

    @Query("UPDATE topMusicData SET year = :year where topMusicId = :id")
    abstract fun updateTopMusicYear(id: Int, year: Int)

    @Insert
    abstract fun insert(topMusicEntity: TopMusicEntity)

    @Query("DELETE from topMusicData where type = :type and weekOf = :weekOf")
    abstract fun deleteForTypeAndWeekOf(type: TopMusicType, weekOf: LocalDate?)

    @Query("DELETE from topMusicData")
    abstract fun deleteAll()
}
