package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import fho.kdvs.global.enums.Quarter
import fho.kdvs.schedule.QuarterYear
import io.reactivex.Flowable
import org.threeten.bp.OffsetDateTime

@Dao
abstract class ShowDao {
    @Query("SELECT * from showData")
    abstract fun allShows(): Flowable<List<ShowEntity>>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC")
    abstract fun allDistinctQuarterYears(): Flowable<List<QuarterYear>>

    @Query("SELECT * from showData WHERE id = :id LIMIT 1")
    abstract fun showById(id: Int): LiveData<ShowEntity>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC LIMIT 1")
    abstract fun currentQuarterYear(): LiveData<QuarterYear>

    //endregion

    @Query("SELECT * from showData")
    abstract fun getAll(): List<ShowEntity>

    @Query("SELECT * from showData WHERE id = :id LIMIT 1")
    abstract fun getShowById(id: Int): ShowEntity?

    @Query("SELECT * from showData WHERE id = :id LIMIT 1")
    abstract fun getShowTimeslotById(id: Int): ShowTimeslotEntity?

    @Query("SELECT * from showData WHERE genre = :genre")
    abstract fun getShowsByGenre(genre: String): List<ShowEntity>

    @Query("SELECT genre from showData ORDER BY genre")
    abstract fun getGenres(): List<String>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC LIMIT 1")
    abstract fun getCurrentQuarterYear(): QuarterYear?

    @Query("SELECT DISTINCT genre from showData ORDER BY genre")
    abstract fun getDistinctGenres(): List<String>

    @Query("SELECT DISTINCT host from showData ORDER BY host")
    abstract fun getDistinctHosts(): List<String>


    @Query("SELECT * from showData WHERE quarter = :quarter AND year = :year")
    abstract fun allShowsByQuarterYear(
        quarter: Quarter,
        year: Int
    ): Flowable<List<ShowEntity>>

    @Query("SELECT * from showData WHERE quarter = :quarter AND year = :year")
    abstract fun getShowsByQuarterYear(
        quarter: Quarter,
        year: Int
    ): List<ShowTimeslotEntity>

    @Transaction
    @Query(
        """SELECT showData.* from showData
        INNER JOIN timeslotData on timeslotData.showId = showData.id
        WHERE (timeStart < :time AND timeEnd > :time OR
        timeEnd < timeStart AND (timeEnd > :time OR timeStart < :time))
        AND quarter = :quarter AND year = :year"""
    )
    abstract fun allShowsAtTime(
        time: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): Flowable<List<ShowTimeslotJoin>>

    @Query(
        """SELECT showData.* from showData
            INNER JOIN timeslotData on timeslotData.showId = showData.id
            WHERE (timeEnd > :timeStart AND timeStart < :timeEnd OR
            timeEnd < timeStart AND (timeEnd > :timeStart OR timeStart < :timeEnd))
            AND quarter = :quarter AND year = :year
            ORDER BY timeStart, quarter, year"""
    )
    abstract fun allShowTimeslotsInTimeRange(
        timeStart: OffsetDateTime,
        timeEnd: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): Flowable<List<ShowTimeslotEntity>>

    @Transaction
    @Query(
        """SELECT showData.* from showData
            INNER JOIN timeslotData on timeslotData.showId = showData.id
            WHERE (timeEnd > :timeStart AND timeStart < :timeEnd OR
            timeEnd < timeStart AND (timeEnd > :timeStart OR timeStart < :timeEnd))
            AND quarter = :quarter AND year = :year
            ORDER BY timeStart, quarter, year"""
    )
    abstract fun allShowTimeslotJoinsInTimeRange(
        timeStart: OffsetDateTime,
        timeEnd: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): Flowable<List<ShowTimeslotJoin>>

    @Transaction
    @Query(
        """SELECT showData.* from showData
            INNER JOIN timeslotData on timeslotData.showId = showData.id
            WHERE (timeEnd > :timeStart AND timeStart < :timeEnd OR
            timeEnd < timeStart AND (timeEnd > :timeStart OR timeStart < :timeEnd))
            AND quarter = :quarter AND year = :year
            ORDER BY timeStart, quarter, year"""
    )
    abstract fun getShowsInTimeRange(
        timeStart: OffsetDateTime,
        timeEnd: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): List<ShowTimeslotJoin>

    @Query(
        """SELECT showData.* from showData
        INNER JOIN timeslotData on timeslotData.showId = showData.id
        WHERE (timeStart <= :time AND timeEnd > :time OR
        timeEnd < timeStart AND (timeEnd > :time OR timeStart < :time))
        AND quarter = :quarter AND year = :year"""
    )
    abstract fun getShowsAtTime(time: OffsetDateTime, quarter: Quarter, year: Int): List<ShowTimeslotEntity>

    @Transaction
    @Query(
        """SELECT showData.* from showData
        INNER JOIN timeslotData on timeslotData.showId = showData.id
        WHERE (timeStart <= :time AND timeEnd > :time OR
        timeEnd < timeStart AND (timeEnd > :time OR timeStart < :time))
        AND quarter = :quarter AND year = :year"""
    )
    abstract fun getShowTimeslotJoinsAtTime(time: OffsetDateTime, quarter: Quarter, year: Int): List<ShowTimeslotJoin>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist"""
    )
    abstract fun getShowsByArtist(artist: String?): List<ShowEntity>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.album = :album"""
    )
    abstract fun getShowsByAlbum(album: String?): List<ShowEntity>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist AND t.album = :album"""
    )
    abstract fun getShowsByArtistAlbum(artist: String?, album: String?): List<ShowEntity>

    @Query(
        """SELECT DISTINCT s.* from showData s
            inner join broadcastData b on b.showId = s.id inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.label = :label"""
    )
    abstract fun getShowsByLabel(label: String?): List<ShowEntity>

    @Insert(onConflict = REPLACE)
    abstract fun insert(showEntity: ShowEntity)

    @Query("DELETE from showData WHERE id = :id")
    abstract fun deleteShow(id: Int)

    @Query("DELETE from showData")
    abstract fun deleteAll()

    /** Updates a show from information pulled from its details page. */
    @Query("UPDATE showData SET host = :host, genre = :genre, defaultDesc = :defaultDesc WHERE id = :id")
    abstract fun updateShowDetails(id: Int, host: String?, genre: String?, defaultDesc: String?)

    @Query("UPDATE showData SET defaultImageHref = :defaultImageHref WHERE id = :id")
    abstract fun updateShowDefaultImageHref(id: Int, defaultImageHref: String?)

    fun updateOrInsert(show: ShowEntity) {
        if (getShowById(show.id) != null) {
            // we don't want to override any existing image hrefs if we didn't find one this time
            show.defaultImageHref?.let { updateShowDefaultImageHref(show.id, it) }
        } else {
            insert(show)
        }
    }
}