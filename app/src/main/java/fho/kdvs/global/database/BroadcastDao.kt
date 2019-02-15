package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface BroadcastDao {

    @Query("SELECT * from broadcastData")
    fun getAll(): List<BroadcastEntity>

    @Query("SELECT * from broadcastData WHERE showId = :showId ORDER BY date DESC")
    fun broadcastsForShowLiveData(showId: Int): LiveData<List<BroadcastEntity>>

    @Query("SELECT * from broadcastData WHERE broadcastId = :broadcastId LIMIT 1")
    fun broadcastById(broadcastId: Int): LiveData<BroadcastEntity>

    @Query("SELECT * from broadcastData WHERE showId = :showId ORDER BY date DESC")
    fun getBroadcastsForShow(showId: Int?): List<BroadcastEntity>

    @Query(
        "SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
                "WHERE t.artist = :artist"
    )
    fun getBroadcastsByArtist(artist: String?): List<BroadcastEntity>

    @Query(
        "SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
                "WHERE t.album = :album"
    )
    fun getBroadcastsByAlbum(album: String?): List<BroadcastEntity>

    @Query(
        "SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
                "WHERE t.artist = :artist AND t.album = :album"
    )
    fun getBroadcastsByArtistAlbum(artist: String?, album: String?): List<BroadcastEntity>

    @Query(
        "SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
                "WHERE t.label = :label"
    )
    fun getBroadcastsByLabel(label: String?): List<BroadcastEntity>

    @Insert(onConflict = REPLACE)
    fun insert(broadcastEntity: BroadcastEntity)

    @Query("DELETE from broadcastData")
    fun deleteAll()

    @Query("DELETE from broadcastData WHERE broadcastId = :broadcastId")
    fun deleteBroadcast(broadcastId: Int?)

    @Query("DELETE from broadcastData WHERE showId = :showId")
    fun deleteBroadcastsForShow(showId: Int?)

    @Query("UPDATE broadcastData SET descr = :descr, imageHref = :imageHref WHERE broadcastId = :broadcastId")
    fun updateBroadcast(broadcastId: Int?, descr: String?, imageHref: String?)
}