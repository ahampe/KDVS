package fho.kdvs.db

import android.database.sqlite.SQLiteConstraintException
import fho.kdvs.MockObjects
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimeslotDaoTest : DatabaseTest() {

    @Test
    fun insert_basic_noShow() {
        val timeslot = MockObjects.showsWithOneTimeslot.first().second
        try {
            db.timeslotDao().insert(timeslot)
            throw AssertionError("Shouldn't be able to insert a timeslot without a show first!")
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun insert_basic() {
        val (show, timeslot) = MockObjects.showsWithOneTimeslot.first()
        db.showDao().insert(show)
        db.timeslotDao().insert(timeslot)

        val timeslotsDb = db.timeslotDao().getAllTimeslots()
        assertEquals(1, timeslotsDb.size)
        assert(timeslotsDb.contains(timeslot))
    }

    @Test
    fun insert_multiple() {
        val shows = MockObjects.showsWithOneTimeslot.map { s -> s.first }
        val timeslots = MockObjects.showsWithOneTimeslot.map { s -> s.second }

        shows.forEach {
            db.showDao().insert(it)
        }

        timeslots.forEach {
            db.timeslotDao().insert(it)
        }

        val timeslotsDb = db.timeslotDao().getAllTimeslots()

        assertEquals(timeslots.size, timeslotsDb.size)
        timeslots.forEach {
            assert(timeslotsDb.contains(it))
        }
    }

    @Test
    fun delete_timeslots() {
        insert_basic()

        try {
            db.timeslotDao().deleteAll()
            val timeslotsDb = db.timeslotDao().getAllTimeslots()
            assertEquals(0, timeslotsDb.size)
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun delete_show_with_cascade_on_timeslot() {
        insert_basic()

        try {
            db.showDao().deleteAll()
            val timeslotsDb = db.timeslotDao().getAllTimeslots()
            assertEquals(0, timeslotsDb.size)
        } catch (e: SQLiteConstraintException) {
        }
    }
}