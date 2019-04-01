package fho.kdvs.global.database

import androidx.room.Insert
import androidx.room.Query

interface TopMusicDao {
    @Query("SELECT * from topMusicData")
    fun getAll(): List<TopMusicEntity>

    @Query("SELECT * from topMusicData where isNewAdd = 1 order by weekOf desc limit 1")
    fun getMostRecentTopAdds(): List<TopMusicEntity>

    @Query("SELECT * from topMusicData where isNewAdd = 0 order by weekOf desc limit 1")
    fun getMostRecentTopAlbums(): List<TopMusicEntity>

    @Insert
    fun insert(topMusicEntity: TopMusicEntity)

    @Query("DELETE from topMusicData")
    fun deleteAll()
}