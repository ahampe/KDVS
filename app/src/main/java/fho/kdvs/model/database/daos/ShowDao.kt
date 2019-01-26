package fho.kdvs.model.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fho.kdvs.model.database.entities.ShowEntity

@Dao
interface ShowDao {
    @Query("SELECT * from showData")
    fun getAll(): List<ShowEntity>

    @Query("SELECT * from showData WHERE id = :id LIMIT 1")
    fun getShowById(id : Int): ShowEntity

    @Query("SELECT * from showData WHERE genre = :genre")
    fun getShowsByGenre(genre: String) : List<ShowEntity>

    @Query("SELECT genre from showData ORDER BY genre")
    fun getGenres() : List<String>

    @Query("SELECT DISTINCT genre from showData ORDER BY genre")
    fun getDistinctGenres() : List<String>

    @Query("SELECT DISTINCT host from showData ORDER BY host")
    fun getDistinctHosts() : List<String>

    @Query("SELECT * from showData where timeStart >= :timeStart " +
            "AND timeEnd <= :timeEnd AND dayOfWeek = :dayOfWeek AND quarter = :quarter AND year = :year")
    fun getShowsInTimeRange(timeStart: String?, timeEnd: String?, dayOfWeek: String?, quarter: String?, year: Int?) : List<ShowEntity>

    @Query("SELECT * from showData where timeStart >= :timeStart " +
            "AND genre = :genre " +
            "AND timeEnd <= :timeEnd AND dayOfWeek = :dayOfWeek AND quarter = :quarter AND year = :year")
    fun getShowsInTimeRangeOfGenre(genre: String?, timeStart: String?, timeEnd: String?, dayOfWeek: String?, quarter: String?, year: Int?) : List<ShowEntity>

    @Query("SELECT DISTINCT s.* from showData s inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.artist = :artist")
    fun getShowsByArtist(artist: String?): List<ShowEntity>

    @Query("SELECT DISTINCT s.* from showData s inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.album = :album")
    fun getShowsByAlbum(album: String?): List<ShowEntity>

    @Query("SELECT DISTINCT s.* from showData s inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.artist = :artist AND t.album = :album")
    fun getShowsByArtistAlbum(artist: String?, album: String?): List<ShowEntity>

    @Query("SELECT DISTINCT s.* from showData s inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId " +
            "WHERE t.label = :label")
    fun getShowsByLabel(label: String?): List<ShowEntity>

    @Insert(onConflict = REPLACE)
    fun insert(showEntity: ShowEntity)

    @Query("DELETE from showData WHERE id = :id")
    fun deleteShow(id: Int?)

    @Query("DELETE from showData")
    fun deleteAll()

    @Query("UPDATE showData SET host = :host, genre = :genre, defaultDesc = :defaultDesc WHERE id = :id")
    fun updateShowInfo(id : Int?, host : String?, genre: String?, defaultDesc : String?)

    @Query("UPDATE showData SET defaultImageHref = :defaultImageHref WHERE id = :id")
    fun updateShowDefaultImageHref(id: Int?, defaultImageHref: String?)
}