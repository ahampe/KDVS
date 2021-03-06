package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import fho.kdvs.global.database.joins.ShowBroadcastFavoriteJoin
import io.reactivex.Flowable

@Dao
interface FavoriteBroadcastDao {
    @Query("SELECT * from favoriteBroadcastData")
    fun allFavoriteBroadcasts(): Flowable<List<FavoriteBroadcastEntity>>

    @Query("SELECT * from favoriteBroadcastData")
    fun getAllFavoriteBroadcasts(): List<FavoriteBroadcastEntity>

    @Transaction
    @Query(
        """SELECT showData.* from favoriteBroadcastData
        INNER JOIN broadcastData on broadcastData.broadcastId = broadcastData.broadcastId
        INNER JOIN showData on showData.id = broadcastData.showId
        INNER JOIN timeslotData on timeslotData.showId = showData.id"""
    )
    fun allTimeslotShowBroadcastFavoriteJoins(): Flowable<List<ShowBroadcastFavoriteJoin>>

    @Query("SELECT * from favoriteBroadcastData where broadcastId = :broadcastId LIMIT 1")
    fun favoriteBroadcastByBroadcastId(broadcastId: Int?): LiveData<FavoriteBroadcastEntity>

    @Query(
        """SELECT favoriteBroadcastId, favoriteBroadcastData.broadcastId from favoriteBroadcastData
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