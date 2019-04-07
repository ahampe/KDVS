package fho.kdvs.global.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
abstract class TopMusicDao {
    @Query("SELECT * from topMusicData")
    abstract fun getAll(): Flowable<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where isNewAdd = 1 order by weekOf desc limit 1")
    abstract fun getMostRecentTopAdds(): Flowable<List<TopMusicEntity>>

    @Query("SELECT * from topMusicData where isNewAdd = 0 order by weekOf desc limit 1")
    abstract fun getMostRecentTopAlbums(): Flowable<List<TopMusicEntity>>

    @Insert
    abstract fun insert(topMusicEntity: TopMusicEntity)

    @Query("DELETE from topMusicData")
    abstract fun deleteAll()
}