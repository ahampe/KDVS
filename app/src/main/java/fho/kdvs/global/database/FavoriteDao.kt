package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface FavoriteDao {
    @Query("SELECT * from favoriteData where trackId = :trackId LIMIT 1")
    fun getByTrackId(trackId: Int?): LiveData<FavoriteEntity>

    @Query(
        """SELECT favoriteId, favoriteData.trackId from favoriteData
        INNER JOIN trackData on favoriteData.trackId = trackData.trackId
        INNER JOIN broadcastData on trackData.broadcastId = broadcastData.broadcastId
        WHERE broadcastData.broadcastId = :broadcastId"""
    )
    fun allFavoritesByBroadcast(broadcastId: Int?): Flowable<List<FavoriteEntity>>

    @Query("SELECT * from favoriteData")
    fun getAll(): List<FavoriteEntity>

    @Insert(onConflict = REPLACE)
    fun insert(FavoriteEntity: FavoriteEntity)

    @Query("DELETE from favoriteData where trackId = :trackId")
    fun deleteByTrackId(trackId: Int?)

    @Query("DELETE from favoriteData")
    fun deleteAll()
}