package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Flowable
import org.threeten.bp.LocalDate

@Dao
abstract class BroadcastDao {
    @Query("SELECT * from broadcastData")
    abstract fun getAll(): List<BroadcastEntity>

    @Query("SELECT * from broadcastData WHERE showId = :showId ORDER BY date DESC")
    abstract fun allBroadcastsForShow(showId: Int): Flowable<List<BroadcastEntity>>

    @Query("SELECT * from broadcastData WHERE showId = :showId ORDER BY date DESC LIMIT 1")
    abstract fun latestBroadcastForShow(showId: Int): LiveData<BroadcastEntity>

    @Query("SELECT * from broadcastData WHERE showId = :showId ORDER BY date DESC LIMIT 1")
    abstract fun getLatestBroadcastForShow(showId: Int): BroadcastEntity?

    @Query(
        """SELECT * from broadcastData
            INNER JOIN showData ON broadcastData.showId = showData.id
            WHERE broadcastId = :broadcastId"""
    )
    abstract fun showByBroadcastId(broadcastId: Int): LiveData<ShowEntity>

    @Query(
        """SELECT * from broadcastData
            INNER JOIN showData ON broadcastData.showId = showData.id
            WHERE broadcastId = :broadcastId"""
    )
    abstract fun getShowByBroadcastId(broadcastId: Int): ShowEntity?

    @Query(
        """SELECT * from broadcastData
            JOIN showData ON broadcastData.showId = showData.id
            WHERE broadcastId = :broadcastId"""
    )
    abstract fun showBroadcastJoinByBroadcastId(broadcastId: Int): LiveData<ShowBroadcastJoin>

    @Query("SELECT * from broadcastData WHERE broadcastId = :broadcastId LIMIT 1")
    abstract fun broadcastById(broadcastId: Int): LiveData<BroadcastEntity>

    @Query("SELECT * from broadcastData WHERE broadcastId = :broadcastId LIMIT 1")
    abstract fun getBroadcastById(broadcastId: Int): BroadcastEntity?

    @Query("SELECT * from broadcastData WHERE showId = :showId ORDER BY date DESC")
    abstract fun getBroadcastsForShow(showId: Int?): List<BroadcastEntity>

    @Query(
        """SELECT DISTINCT b.* from broadcastData b
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist"""
    )
    abstract fun getBroadcastsByArtist(artist: String?): List<BroadcastEntity>

    @Query(
        """SELECT DISTINCT b.* from broadcastData b
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.album = :album"""
    )
    abstract fun getBroadcastsByAlbum(album: String?): List<BroadcastEntity>

    @Query(
        """SELECT DISTINCT b.* from broadcastData b
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist AND t.album = :album"""
    )
    abstract fun getBroadcastsByArtistAlbum(artist: String?, album: String?): List<BroadcastEntity>

    @Query(
        """SELECT DISTINCT b.* from broadcastData b
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.label = :label"""
    )
    abstract fun getBroadcastsByLabel(label: String?): List<BroadcastEntity>

    @Insert(onConflict = REPLACE)
    abstract fun insert(broadcastEntity: BroadcastEntity)

    @Query("DELETE from broadcastData")
    abstract fun deleteAll()

    @Query("DELETE from broadcastData WHERE broadcastId = :broadcastId")
    abstract fun deleteBroadcast(broadcastId: Int?)

    @Query("DELETE from broadcastData WHERE showId = :showId")
    abstract fun deleteBroadcastsForShow(showId: Int?)

    /** Updates a broadcast from information only visible from the show details page. */
    @Query(
        """UPDATE broadcastData
        SET showId = :showId, date = :date
        WHERE broadcastId = :broadcastId
    """
    )
    abstract fun updateBroadcastInfo(broadcastId: Int, showId: Int, date: LocalDate?)

    /** Updates a broadcast with info retrieved from its details page. */
    @Query(
        """UPDATE broadcastData
        SET description = :description, imageHref = :imageHref
        WHERE broadcastId = :broadcastId"""
    )
    abstract fun updateBroadcastDetails(broadcastId: Int?, description: String?, imageHref: String?)

    fun updateOrInsert(broadcast: BroadcastEntity) {
        if (getBroadcastById(broadcast.broadcastId) != null) {
            updateBroadcastInfo(broadcast.broadcastId, broadcast.showId, broadcast.date)
        } else {
            insert(broadcast)
        }
    }
}