package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fho.kdvs.global.web.MusicBrainzReleaseData
import fho.kdvs.global.web.SpotifyData
import fho.kdvs.topmusic.TopMusicType
import org.threeten.bp.LocalDate

@Dao
abstract class TopMusicDao {
    @Query("SELECT * from topMusicData")
    abstract fun getAll(): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where type = :type order by weekOf desc limit :limit")
    abstract fun getMostRecentTopMusicForType(type: TopMusicType, limit: Int): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where type = :type and weekOf = :weekOf")
    abstract fun getTopMusicForWeekOfType(weekOf: LocalDate?, type: TopMusicType): LiveData<List<TopMusicEntity>>

    @Query("UPDATE topMusicData SET musicBrainzData = :mbData where topMusicId = :id")
    abstract fun updateTopMusicMusicBrainzData(id: Int, mbData: MusicBrainzReleaseData)

    @Query("UPDATE topMusicData SET spotifyData = :spotifyData where topMusicId = :id")
    abstract fun updateTopMusicSpotifyData(id: Int, spotifyData: SpotifyData)

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