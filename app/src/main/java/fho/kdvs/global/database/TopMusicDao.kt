package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class TopMusicDao {
    @Query("SELECT * from topMusicData")
    abstract fun getAll(): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where isNewAdd = 1 order by weekOf desc limit 1")
    abstract fun getMostRecentTopAdds(): LiveData<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where isNewAdd = 0 order by weekOf desc limit 1")
    abstract fun getMostRecentTopAlbums(): LiveData<List<TopMusicEntity>>

    @Insert
    abstract fun insert(topMusicEntity: TopMusicEntity)

    @Query("DELETE from topMusicData")
    abstract fun deleteAll()
}