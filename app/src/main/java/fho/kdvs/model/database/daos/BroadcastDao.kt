package fho.kdvs.model.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fho.kdvs.model.database.entities.BroadcastEntity

@Dao
interface BroadcastDao {

    @Query("SELECT * from broadcastData")
    fun getAll(): List<BroadcastEntity>

    @Query("SELECT * from broadcastData WHERE showId = :showId")
    fun getBroadcastsForShow(showId : Int?): List<BroadcastEntity>

    @Query("SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.artist = :artist")
    fun getBroadcastsByArtist(artist: String?): List<BroadcastEntity>

    @Query("SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.album = :album")
    fun getBroadcastsByAlbum(album: String?): List<BroadcastEntity>

    @Query("SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.artist = :artist AND t.album = :album")
    fun getBroadcastsByArtistAlbum(artist: String?, album: String?): List<BroadcastEntity>

    @Query("SELECT DISTINCT b.* from broadcastData b inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.label = :label")
    fun getBroadcastsByLabel(label: String?): List<BroadcastEntity>

    @Insert(onConflict = REPLACE)
    fun insert(broadcastEntity: BroadcastEntity)

    @Query("DELETE from broadcastData")
    fun deleteAll()

    @Query("UPDATE broadcastData SET desc = :desc, imageHref = :imageHref WHERE broadcastId = :broadcastId")
    fun updateBroadcast(broadcastId: Int?, desc: String?, imageHref: String?)
}