package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import fho.kdvs.global.database.joins.ShowBroadcastTrackFavoriteJoin
import io.reactivex.Flowable

@Dao
interface FavoriteTrackDao {
    @Query("SELECT * from favoriteTrackData")
    fun getAll(): Flowable<List<FavoriteTrackEntity>>

    @Query("SELECT * from favoriteTrackData")
    fun getAllFavoriteTracks(): List<FavoriteTrackEntity>

    @Transaction
    @Query(
        """SELECT showData.* from favoriteTrackData
        INNER JOIN trackData on favoriteTrackData.trackId = trackData.trackId
        INNER JOIN broadcastData on broadcastData.broadcastId = trackData.broadcastId
        INNER JOIN showData on showData.id = broadcastData.showId
        INNER JOIN timeslotData on timeslotData.showId = showData.id"""
    )
    fun allShowBroadcastTrackFavoriteJoins(): Flowable<List<ShowBroadcastTrackFavoriteJoin>>

    @Query("SELECT * from favoriteTrackData where trackId = :trackId LIMIT 1")
    fun getByTrackId(trackId: Int?): LiveData<FavoriteTrackEntity>

    @Query(
        """SELECT favoriteTrackId, favoriteTrackData.trackId from favoriteTrackData
        INNER JOIN trackData on favoriteTrackData.trackId = trackData.trackId
        INNER JOIN broadcastData on trackData.broadcastId = broadcastData.broadcastId
        WHERE broadcastData.broadcastId = :broadcastId"""
    )
    fun allFavoritesByBroadcast(broadcastId: Int?): Flowable<List<FavoriteTrackEntity>>

    @Insert(onConflict = REPLACE)
    fun insert(FavoriteEntity: FavoriteTrackEntity)

    @Query("DELETE from favoriteTrackData where trackId = :trackId")
    fun deleteByTrackId(trackId: Int?)

    @Query("DELETE from favoriteTrackData")
    fun deleteAll()
}