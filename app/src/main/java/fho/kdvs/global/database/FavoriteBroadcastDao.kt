package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface FavoriteBroadcastDao {
    @Query("SELECT * from favoriteBroadcastData")
    fun getAll(): Flowable<List<FavoriteBroadcastEntity>>

    @Query("""SELECT favoriteBroadcastData.*, broadcastData.*, showData.* from favoriteBroadcastData
        INNER JOIN broadcastData on broadcastData.broadcastId = broadcastData.broadcastId
        INNER JOIN showData on showData.id = broadcastData.showId"""
    )
    fun allShowBroadcastFavoriteJoins(): Flowable<List<ShowBroadcastFavoriteJoin>>

    @Query("SELECT * from favoriteBroadcastData where broadcastId = :broadcastId LIMIT 1")
    fun getByBroadcastId(broadcastId: Int?): LiveData<FavoriteBroadcastEntity>

    @Query(
        """SELECT favoriteId, favoriteBroadcastData.broadcastId from favoriteBroadcastData
        INNER JOIN broadcastData on broadcastData.broadcastId = broadcastData.broadcastId
        WHERE broadcastData.broadcastId = :broadcastId"""
    )
    fun allFavoritesByBroadcast(broadcastId: Int?): Flowable<List<FavoriteBroadcastEntity>>

    @Insert(onConflict = REPLACE)
    fun insert(FavoriteEntity: FavoriteBroadcastEntity)

    @Query("DELETE from favoriteBroadcastData where broadcastId = :broadcastId")
    fun deleteByBroadcastId(broadcastId: Int?)

    @Query("DELETE from favoriteBroadcastData")
    fun deleteAll()
}