package fho.kdvs.model.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.ShowEntity
import io.reactivex.Flowable
import java.util.*

@Dao
interface ShowDao {
    @Query("SELECT * from showData")
    fun allShows(): Flowable<List<ShowEntity>>

    @Query("SELECT * from showData WHERE dayOfWeekStart = :dayOfWeek OR dayOfWeekEnd = :dayOfWeek ORDER BY timeStart, dayOfWeekStart")
    fun allShowsForDay(dayOfWeek: Day): Flowable<List<ShowEntity>>

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

    @Query("""SELECT * from showData WHERE
            (dayOfWeekStart = :dayOfWeekStart AND timeStart >= :timeStart AND
               ((dayOfWeekEnd = :dayOfWeekEnd AND timeEnd <= :timeEnd) OR
                ((dayOfWeekEnd < :dayOfWeekEnd) OR (dayOfWeekEnd = 6 AND :dayOfWeekEnd = 0)))) OR
            ((dayOfWeekStart > :dayOfWeekStart OR (dayOfWeekStart = 0 AND :dayOfWeekStart = 6)) AND
              ((timeEnd <= :timeEnd) OR
              ((dayOfWeekEnd < :dayOfWeekEnd OR (dayOfWeekEnd = 6 AND :dayOfWeekEnd = 0)))))
            AND quarter >= :quarterStart AND quarter <= :quarterEnd
            AND year >= :yearStart AND year <= :yearEnd
            ORDER BY timeStart, dayOfWeekStart, quarter, year""")
    fun getShowsInTimeRange(timeStart: Date, timeEnd: Date, dayOfWeekStart: Day, dayOfWeekEnd: Day,
                            quarterStart: Quarter, quarterEnd: Quarter, yearStart: Int, yearEnd: Int) : List<ShowEntity>

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