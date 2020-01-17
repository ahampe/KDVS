package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import fho.kdvs.global.database.joins.ShowTimeslotsJoin
import fho.kdvs.global.enums.Quarter
import fho.kdvs.schedule.QuarterYear
import io.reactivex.Flowable
import org.threeten.bp.OffsetDateTime

@Dao
abstract class ShowDao {
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * from showData INNER JOIN timeslotData on timeslotData.showId = showData.id")
    abstract fun allShowTimeslots(): Flowable<List<ShowTimeslotEntity>>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * from showData INNER JOIN timeslotData on timeslotData.showId = showData.id")
    abstract fun allShowTimeslotsJoins(): Flowable<List<ShowTimeslotsJoin>>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC")
    abstract fun allDistinctQuarterYears(): Flowable<List<QuarterYear>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""SELECT * from showData
            INNER JOIN timeslotData on timeslotData.showId = showData.id 
            WHERE id = :id LIMIT 1""")
    abstract fun showTimeslotById(id: Int): LiveData<ShowTimeslotEntity>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""SELECT * from showData
            INNER JOIN timeslotData on timeslotData.showId = showData.id 
            WHERE id = :id LIMIT 1""")
    abstract fun showTimeslotsJoinById(id: Int): LiveData<ShowTimeslotsJoin>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""SELECT timeslotData.* from showData
            INNER JOIN timeslotData on timeslotData.showId = showData.id 
            WHERE showId = :showId""")
    abstract fun timeslotsById(showId: Int): LiveData<List<TimeslotEntity>>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC LIMIT 1")
    abstract fun currentQuarterYear(): LiveData<QuarterYear>

    //endregion

    @Query("SELECT * from showData")
    abstract fun getAllShows(): List<ShowEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * from showData INNER JOIN timeslotData on timeslotData.showId = showData.id")
    abstract fun getAllShowTimeslots(): List<ShowTimeslotEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("""SELECT * from showData 
        INNER JOIN timeslotData on timeslotData.showId = showData.id 
        WHERE id = :id LIMIT 1""")
    abstract fun getShowTimeslotById(id: Int): ShowTimeslotEntity?

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("""SELECT * from showData 
        INNER JOIN timeslotData on timeslotData.showId = showData.id 
        WHERE id = :id LIMIT 1""")
    abstract fun getShowTimeslotJoinsById(id: Int): ShowTimeslotsJoin?

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""SELECT * from showData 
        INNER JOIN timeslotData on timeslotData.showId = showData.id
        WHERE genre = :genre""")
    abstract fun getShowTimeslotsByGenre(genre: String): List<ShowTimeslotEntity>

    @Query("SELECT genre from showData ORDER BY genre")
    abstract fun getGenres(): List<String>

    @Query("SELECT DISTINCT quarter, year from showData ORDER BY year DESC, quarter DESC LIMIT 1")
    abstract fun getCurrentQuarterYear(): QuarterYear?

    @Query("SELECT DISTINCT genre from showData ORDER BY genre")
    abstract fun getDistinctGenres(): List<String>

    @Query("SELECT DISTINCT host from showData ORDER BY host")
    abstract fun getDistinctHosts(): List<String>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""SELECT * from showData 
        INNER JOIN timeslotData on timeslotData.showId = showData.id 
        WHERE quarter = :quarter AND year = :year""")
    abstract fun allShowTimeslotsByQuarterYear(
        quarter: Quarter,
        year: Int
    ): Flowable<List<ShowTimeslotEntity>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""SELECT * from showData 
        INNER JOIN timeslotData on timeslotData.showId = showData.id 
        WHERE quarter = :quarter AND year = :year""")
    abstract fun getShowTimeslotsByQuarterYear(
        quarter: Quarter,
        year: Int
    ): List<ShowTimeslotEntity>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""SELECT * from showData 
        INNER JOIN timeslotData on timeslotData.showId = showData.id 
        WHERE quarter = :quarter AND year = :year""")
    abstract fun getShowTimeslotJoinsByQuarterYear(
        quarter: Quarter,
        year: Int
    ): List<ShowTimeslotsJoin>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        """SELECT * from showData
        INNER JOIN timeslotData on timeslotData.showId = showData.id
        WHERE (timeStart < :time AND timeEnd > :time OR
        timeEnd < timeStart AND (timeEnd > :time OR timeStart < :time))
        AND quarter = :quarter AND year = :year
        ORDER BY timeStart, quarter, year"""
    )
    abstract fun allShowTimeslotsAtTime(
        time: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): Flowable<List<ShowTimeslotEntity>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """SELECT * from showData
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

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        """SELECT * from showData
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
    ): Flowable<List<ShowTimeslotsJoin>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        """SELECT * from showData
            INNER JOIN timeslotData on timeslotData.showId = showData.id
            WHERE (timeEnd > :timeStart AND timeStart < :timeEnd OR
            timeEnd < timeStart AND (timeEnd > :timeStart OR timeStart < :timeEnd))
            AND quarter = :quarter AND year = :year
            ORDER BY timeStart, quarter, year"""
    )
    abstract fun getShowTimeslotsInTimeRange(
        timeStart: OffsetDateTime,
        timeEnd: OffsetDateTime,
        quarter: Quarter,
        year: Int
    ): List<ShowTimeslotEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """SELECT * from showData
        INNER JOIN timeslotData on timeslotData.showId = showData.id
        WHERE (timeStart <= :time AND timeEnd > :time OR
        timeEnd < timeStart AND (timeEnd > :time OR timeStart < :time))
        AND quarter = :quarter AND year = :year"""
    )
    abstract fun getShowTimeslotsAtTime(time: OffsetDateTime, quarter: Quarter, year: Int): List<ShowTimeslotEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        """SELECT * from showData
        INNER JOIN timeslotData on timeslotData.showId = showData.id
        WHERE (timeStart <= :time AND timeEnd > :time OR
        timeEnd < timeStart AND (timeEnd > :time OR timeStart < :time))
        AND quarter = :quarter AND year = :year"""
    )
    abstract fun getShowTimeslotJoinsAtTime(time: OffsetDateTime, quarter: Quarter, year: Int): List<ShowTimeslotsJoin>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """SELECT DISTINCT s.*, ti.* from showData s
            INNER JOIN timeslotData ti on ti.showId = s.id
            inner join broadcastData b on b.showId = s.id 
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist"""
    )
    abstract fun getShowTimeslotsByArtist(artist: String?): List<ShowTimeslotEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """SELECT DISTINCT s.*, ti.* from showData s
            INNER JOIN timeslotData ti on ti.showId = s.id
            inner join broadcastData b on b.showId = s.id 
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.album = :album"""
    )
    abstract fun getShowTimeslotsByAlbum(album: String?): List<ShowTimeslotEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """SELECT DISTINCT s.*, ti.* from showData s
            INNER JOIN timeslotData ti on ti.showId = s.id
            inner join broadcastData b on b.showId = s.id 
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.artist = :artist AND t.album = :album"""
    )
    abstract fun getShowTimeslotsByArtistAlbum(artist: String?, album: String?): List<ShowTimeslotEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """SELECT DISTINCT s.*, ti.* from showData s
            INNER JOIN timeslotData ti on ti.showId = s.id
            inner join broadcastData b on b.showId = s.id 
            inner join trackData t on t.broadcastId = b.broadcastId
            WHERE t.label = :label"""
    )
    abstract fun getShowTimeslotsByLabel(label: String?): List<ShowTimeslotEntity>

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

    // TODO change logic for multi-timeslots
    fun updateOrInsert(show: ShowEntity) {
        if (getShowTimeslotById(show.id) != null) {
            // we don't want to override any existing image hrefs if we didn't find one this time
            show.defaultImageHref?.let { updateShowDefaultImageHref(show.id, it) }
        } else {
            insert(show)
        }
    }
}