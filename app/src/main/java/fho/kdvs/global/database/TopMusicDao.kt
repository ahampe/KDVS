package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class TopMusicDao {
    @Query("SELECT * from topMusicData")
    abstract fun getAll(): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where isNewAdd = 1 order by weekOf desc limit 5")
    abstract fun getMostRecentTopAdds(): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where isNewAdd = 0 order by weekOf desc limit 30")
    abstract fun getMostRecentTopAlbums(): LiveData<List<TopMusicEntity>>

    @Query("UPDATE topMusicData SET album = :album where topMusicId = :id")
    abstract fun updateTopMusicAlbum(id: Int, album: String)

    @Query("UPDATE topMusicData SET label = :label where topMusicId = :id")
    abstract fun updateTopMusicLabel(id: Int, label: String)

    @Query("UPDATE topMusicData SET imageHref = :imageHref where topMusicId = :id")
    abstract fun updateTopMusicImageHref(id: Int, imageHref: String)

    @Query("UPDATE topMusicData SET year = :year where topMusicId = :id")
    abstract fun updateTopMusicYear(id: Int, year: Int)

    @Query("UPDATE topMusicData SET spotifyUri = :spotifyUri where topMusicId = :id")
    abstract fun updateTopMusicSpotifyUri(id: Int, spotifyUri: String)

    @Insert
    abstract fun insert(topMusicEntity: TopMusicEntity)

    @Query("DELETE from topMusicData")
    abstract fun deleteAll()
}