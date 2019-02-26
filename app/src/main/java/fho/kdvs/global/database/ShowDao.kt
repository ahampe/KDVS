package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fho.kdvs.global.enums.Quarter
import fho.kdvs.schedule.QuarterYear
import io.reactivex.Flowable
import org.threeten.bp.OffsetDateTime

@Dao
interface ShowDao {
    @Query("SELECT * from showData")
    fun allShows(): Flowable<List<ShowEntity>>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC")
    fun allDistinctQuarterYears(): Flowable<List<QuarterYear>>

    @Query("SELECT * from showData WHERE id = :id LIMIT 1")
    fun showById(id: Int): LiveData<ShowEntity>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC LIMIT 1")
    fun currentQuarterYear(): LiveData<QuarterYear>

    //endregion

    @Query("SELECT * from showData")
    fun getAll(): List<ShowEntity>

    @Query("SELECT * from showData WHERE id = :id LIMIT 1")
    fun getShowById(id: Int): ShowEntity

    @Query("SELECT * from showData WHERE genre = :genre")
    fun getShowsByGenre(genre: String): List<ShowEntity>

    @Query("SELECT genre from showData ORDER BY genre")
    fun getGenres(): List<String>

    @Query("SELECT DISTINCT genre from showData ORDER BY genre")
    fun getDistinctGenres(): List<String>

    @Query("SELECT DISTINCT host from showData ORDER BY host")
    fun getDistinctHosts(): List<String>

    @Query(
        """SELECT * from showData
            WHERE (timeEnd > :timeStart AND timeStart < :timeEnd OR
            timeEnd < timeStart AND (timeEnd > :timeStart OR timeStart < :timeEnd))
            AND quarter = :quarter AND year = :year
            ORDER BY timeStart, quarter, year"""
    )
    fun allShowsInTimeRange(
        timeStart: OffsetDateTime,
        timeEnd: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): Flowable<List<ShowEntity>>

    @Query(
        """SELECT * from showData
            WHERE (timeEnd > :timeStart AND timeStart < :timeEnd OR
            timeEnd < timeStart AND (timeEnd > :timeStart OR timeStart < :timeEnd))
            AND quarter = :quarter AND year = :year
            ORDER BY timeStart, quarter, year"""
    )
    fun getShowsInTimeRange(
        timeStart: OffsetDateTime,
        timeEnd: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): List<ShowEntity>

    @Query(
        """SELECT * from showData
        WHERE (timeStart < :time AND timeEnd > :time OR
        timeEnd < timeStart AND (timeEnd > :time OR timeStart < :time))
        AND quarter = :quarter AND year = :year"""
    )
    fun getShowsAtTime(time: OffsetDateTime, quarter: Quarter, year: Int): List<ShowEntity>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist"""
    )
    fun getShowsByArtist(artist: String?): List<ShowEntity>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.album = :album"""
    )
    fun getShowsByAlbum(album: String?): List<ShowEntity>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist AND t.album = :album"""
    )
    fun getShowsByArtistAlbum(artist: String?, album: String?): List<ShowEntity>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.label = :label"""
    )
    fun getShowsByLabel(label: String?): List<ShowEntity>

    @Insert(onConflict = REPLACE)
    fun insert(showEntity: ShowEntity)

    @Query("DELETE from showData WHERE id = :id")
    fun deleteShow(id: Int?)

    @Query("DELETE from showData")
    fun deleteAll()

    @Query("UPDATE showData SET host = :host, genre = :genre, defaultDesc = :defaultDesc WHERE id = :id")
    fun updateShowInfo(id: Int?, host: String?, genre: String?, defaultDesc: String?)

    @Query("UPDATE showData SET defaultImageHref = :defaultImageHref WHERE id = :id")
    fun updateShowDefaultImageHref(id: Int?, defaultImageHref: String?)
}