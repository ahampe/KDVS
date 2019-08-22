package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fho.kdvs.global.web.MusicBrainzReleaseData
import fho.kdvs.global.web.SpotifyData
import io.reactivex.Flowable

@Dao
interface TrackDao {

    @Query("SELECT * from trackData WHERE trackId = :trackId LIMIT 1")
    fun trackById(trackId: Int?): LiveData<TrackEntity>

    @Query("SELECT * from trackData WHERE broadcastId = :broadcastId ORDER BY position")
    fun allTracksForBroadcast(broadcastId: Int?): Flowable<List<TrackEntity>>

    @Query("SELECT * from trackData WHERE broadcastId = :broadcastId AND airbreak = 0 ORDER BY position")
    fun allSongsForBroadcast(broadcastId: Int?): Flowable<List<TrackEntity>>

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

    @Insert(onConflict = REPLACE)
    fun insert(trackEntity: TrackEntity)

    @Query("UPDATE trackData SET album = :album WHERE trackId = :id")
    fun updateAlbum(id: Int?, album: String?)

    @Query("UPDATE trackData SET imageHref = :href WHERE trackId = :id")
    fun updateImageHref(id: Int?, href: String?)

    @Query("UPDATE trackData SET label= :label WHERE trackId = :id")
    fun updateLabel(id: Int?, label: String?)

    @Query("UPDATE trackData SET musicBrainzData = :musicBrainzData WHERE trackId = :id")
    fun updateMusicBrainzData(id: Int?, musicBrainzData: MusicBrainzReleaseData?)

    @Query("UPDATE trackData SET spotifyData = :spotifyData WHERE trackId = :id")
    fun updateSpotifyData(id: Int?, spotifyData: SpotifyData?)

    @Query("UPDATE trackData SET year = :year WHERE trackId = :id")
    fun updateYear(id: Int?, year: Int?)

    @Query("DELETE from trackData where broadcastId = :broadcastId")
    fun deleteByBroadcast(broadcastId: Int?)

    @Query("DELETE from trackData")
    fun deleteAll()
}