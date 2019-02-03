package fho.kdvs.global.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface TrackDao {

    @Query("SELECT * from trackData")
    fun getAll(): List<TrackEntity>

    @Query("SELECT * from trackData WHERE broadcastId = :broadcastId")
    fun getTracksForBroadcast(broadcastId: Int?): List<TrackEntity>

    @Query(
        "SELECT * from trackData t " +
                "inner join broadcastData b on b.broadcastId = t.broadcastId " +
                "inner join showData s on b.showId = s.id " +
                "WHERE s.id = :showId AND t.airbreak = 0"
    )
    fun getTracksByShow(showId: Int?): List<TrackEntity>

    @Query("SELECT * from trackData WHERE artist like :artist")
    fun getTracksByArtist(artist: String?): List<TrackEntity>

    @Query("SELECT * from trackData WHERE song like :song")
    fun getTracksBySong(song: String?): List<TrackEntity>

    @Query("SELECT * from trackData WHERE album like :album")
    fun getTracksByAlbum(album: String?): List<TrackEntity>

    @Query("SELECT * from trackData WHERE artist like :artist AND album like :album")
    fun getTracksByArtistAlbum(artist: String?, album: String?): List<TrackEntity>

    @Query("SELECT * from trackData WHERE label like :label")
    fun getTracksByLabel(label: String?): List<TrackEntity>

    //TODO: fuzzy search for music metadata?

    @Insert(onConflict = REPLACE)
    fun insert(trackEntity: TrackEntity)

    @Query("DELETE from trackData where broadcastId = :broadcastId")
    fun deleteByBroadcast(broadcastId: Int?)

    @Query("DELETE from trackData")
    fun deleteAll()
}