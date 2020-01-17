package fho.kdvs.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fho.kdvs.DbTestUtils
import fho.kdvs.global.database.ShowTimeslotEntity
import fho.kdvs.global.database.makeShowTimeslot
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.util.TimeHelper
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ShowDaoTest : DatabaseTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val showDao by lazy { db.showDao() }
    private val timeslotDao by lazy { db.timeslotDao() }
    private lateinit var showTimeslotsQueue: LinkedBlockingQueue<List<ShowTimeslotEntity>>
    private var compositeDisposable = CompositeDisposable()

    @Before
    fun setup() {
        showTimeslotsQueue = LinkedBlockingQueue()
    }

    @After
    fun tear_down() {
        compositeDisposable.clear()
    }

    @Test
    fun insert_basic_show_and_timeslot() {
        val (show, timeslot) = DbTestUtils.createShowsWithOneTimeslot().first()

        showDao.insert(show)
        timeslotDao.insert(timeslot)

        val showsDb = showDao.getAllShows()
        val timeslotsDb = timeslotDao.getAllTimeslots()

        assertTrue(showsDb.contains(show))
        assertTrue(timeslotsDb.contains(timeslot))
        assertEquals(1, showsDb.size)
        assertEquals(1, timeslotsDb.size)
    }

    @Test
    fun insert_basic_show_and_timeslots() {
        val (show, timeslots) = DbTestUtils.createShowsWithMultipleTimeslots().first()

        showDao.insert(show)

        timeslots.forEach {
            timeslotDao.insert(it)
        }

        val showsDb = showDao.getAllShows()
        val timeslotsDb = timeslotDao.getAllTimeslots()

        assertTrue(showsDb.contains(show))
        timeslots.forEach {
            assertTrue(timeslotsDb.contains(it))
        }

        assertEquals(1, showsDb.size)
        assertEquals(timeslots.size, timeslotsDb.size)
    }

    @Test
    fun insert_multiple_shows_with_one_timeslot_each() {
        val showsWithTimeslots = DbTestUtils.createShowsWithOneTimeslot()

        showsWithTimeslots.forEach { (show, timeslot) ->
            showDao.insert(show)
            timeslotDao.insert(timeslot)
        }

        val showsDb = showDao.getAllShows()
        val timeslotsDb = timeslotDao.getAllTimeslots()

        assertEquals(showsWithTimeslots.size, showsDb.size)
        assertEquals(showsWithTimeslots.size, timeslotsDb.size)

        showsWithTimeslots.forEach { (show, timeslot) ->
            assertTrue(showsDb.contains(show))
            assertTrue(timeslotsDb.contains(timeslot))
        }
    }

    @Test
    fun insert_multiple_shows_multiple_timeslots_each() {
        val showsWithTimeslots = DbTestUtils.createShowsWithMultipleTimeslots()
        val shows = showsWithTimeslots.map { s -> s.first }
        val timeslots = showsWithTimeslots.flatMap { s -> s.second }

        showsWithTimeslots.forEach { (show, timeslots) ->
            showDao.insert(show)

            timeslots.forEach {
                timeslotDao.insert(it)
            }
        }

        val showsDb = showDao.getAllShows()
        val timeslotsDb = timeslotDao.getAllTimeslots()

        assertEquals(shows.size, showsDb.size)
        assertEquals(timeslots.size, timeslotsDb.size)

        showsWithTimeslots.forEach { (show, timeslots) ->
            assertTrue(showsDb.contains(show))

            timeslots.forEach {
                assertTrue(timeslotsDb.contains(it))
            }
        }
    }

    @Test
    fun get_all_shows_for_time_range() {
        val showTimeslots = DbTestUtils.createShowsWithOneTimeslot()
        val shows = showTimeslots.map { s -> s.first }

        showTimeslots.forEach { (show, timeslot) ->
            showDao.insert(show)
            timeslotDao.insert(timeslot)
        }

        val timeStart = TimeHelper.makeWeekTime24h("00:00", Day.SUNDAY)
        val timeEnd = TimeHelper.makeWeekTime24h("03:00", Day.SUNDAY)

        val showsDb = showDao.getShowTimeslotsInTimeRange(timeStart, timeEnd, Quarter.SPRING, 1943)

        assertEquals(shows.size, showsDb.size)
        showTimeslots.forEach {
            assert(showsDb.contains(makeShowTimeslot(it.first, it.second)))
        }
    }

    @Test
    fun delete_show() {
        val showTimeslots = DbTestUtils.createShowsWithOneTimeslot()
        showTimeslots.forEach { (show, timeslot) ->
            showDao.insert(show)
            timeslotDao.insert(timeslot)
        }

        showDao.deleteShow(showTimeslots.first().first.id)
        val showsDb = showDao.getAllShowTimeslots()
        val timeslotsDb = timeslotDao.getAllTimeslots()
        assertEquals("delete show failed", 0, showsDb.filter { it.id == showTimeslots.first().first.id }.size)
        assertEquals("delete show failed", showTimeslots.size - 1, showsDb.size)
        assertEquals("delete timeslot failed", 0, timeslotsDb.filter { it.showId == showTimeslots.first().first.id }.size)
        assertEquals("delete timeslot failed", showTimeslots.size - 1, timeslotsDb.size)
    }

    @Test
    fun update_show_info() {
        val host = "Varg"
        val genre = "Prison MIDI"
        val defaultDesc = "Let's find out"

        insert_basic_show_and_timeslot()

        var showDb = showDao.getAllShowTimeslots().first()
        showDao.updateShowDetails(showDb.id, host, genre, defaultDesc)
        showDb = showDao.getAllShowTimeslots().first()

        assertEquals("host not updated", host, showDb.host)
        assertEquals("genre not updated", genre, showDb.genre)
        assertEquals("defaultDesc not updated", defaultDesc, showDb.defaultDesc)
    }

    @Test
    fun update_show_default_imagehref() {
        val imageHref = "www.test.com/image.png"

        insert_basic_show_and_timeslot()

        var showDb = showDao.getAllShowTimeslots().first()
        showDao.updateShowDefaultImageHref(showDb.id, imageHref)
        showDb = showDao.getAllShowTimeslots().first()

        assertEquals("image not updated", imageHref, showDb.defaultImageHref)
    }

    @Test
    fun observable_insert() {
        compositeDisposable += observeShowTimeslots(showDao.allShowTimeslots())

        val (show, timeslot) = DbTestUtils.createShowsWithOneTimeslot().first()
        showDao.insert(show)
        timeslotDao.insert(timeslot)

        val showTimeslots = showTimeslotsQueue.poll(defaultTimeOut, TimeUnit.SECONDS)

        assertEquals("should have exactly 1 show", 1, showTimeslots.size)
        assertEquals("should only have emitted once", 0, showTimeslotsQueue.size)
    }

    private fun observeShowTimeslots(flowable: Flowable<List<ShowTimeslotEntity>>) =
        flowable.skipWhile { it.isNullOrEmpty() }
            .subscribe { showTimeslots ->
                showTimeslotsQueue.add(showTimeslots)
            }
}