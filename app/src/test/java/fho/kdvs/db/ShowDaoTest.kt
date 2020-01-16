package fho.kdvs.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fho.kdvs.DbTestUtils
import fho.kdvs.global.database.ShowEntity
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
    private lateinit var showsQueue: LinkedBlockingQueue<List<ShowEntity>>
    private var compositeDisposable = CompositeDisposable()

    @Before
    fun setup() {
        showsQueue = LinkedBlockingQueue()
    }

    @After
    fun tear_down() {
        compositeDisposable.clear()
    }

    @Test
    fun insert_basic() {
        val show = DbTestUtils.createShows().first()
        showDao.insert(show)

        val showsDb = showDao.getAllShowTimeslots()
        assertTrue(showsDb.contains(show))
        assertEquals(1, showsDb.size)
    }

    @Test
    fun insert_multiple() {
        val shows = DbTestUtils.createShows()
        shows.forEach {
            showDao.insert(it)
        }

        val showsDb = showDao.getAllShowTimeslots()
        assertEquals(shows.size, showsDb.size)
        shows.forEach {
            assertTrue(showsDb.contains(it))
        }
    }

    @Test
    fun get_all_shows_for_time_range() {
        val shows = DbTestUtils.createShows()
        shows.forEach {
            showDao.insert(it)
        }

        val timeStart = TimeHelper.makeWeekTime24h("00:00", Day.SUNDAY)
        val timeEnd = TimeHelper.makeWeekTime24h("03:00", Day.SUNDAY)

        val showsDb = showDao.getShowTimeslotsInTimeRange(timeStart, timeEnd, Quarter.SPRING, 1943)

        assertEquals(shows.size, showsDb.size)
        shows.forEach {
            assert(showsDb.contains(it))
        }
    }

    @Test
    fun delete_show() {
        val shows = DbTestUtils.createShows()
        shows.forEach {
            showDao.insert(it)
        }

        showDao.deleteShow(shows.first().id)
        val showsDb = showDao.getAllShowTimeslots()
        assertEquals("delete show failed", 0, showsDb.filter { it.id == shows.first().id }.size)
        assertEquals("delete show failed", shows.size - 1, showsDb.size)
    }

    @Test
    fun update_show_info() {
        val host = "Varg"
        val genre = "Prison MIDI"
        val defaultDesc = "Let's find out"

        insert_basic()

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

        insert_basic()

        var showDb = showDao.getAllShowTimeslots().first()
        showDao.updateShowDefaultImageHref(showDb.id, imageHref)
        showDb = showDao.getAllShowTimeslots().first()

        assertEquals("image not updated", imageHref, showDb.defaultImageHref)
    }

    @Test
    fun observable_insert() {
        compositeDisposable += observeShows(showDao.allShowTimeslots())

        val show = DbTestUtils.createShows().first()
        showDao.insert(show)

        val shows = showsQueue.poll(defaultTimeOut, TimeUnit.SECONDS)
        assertEquals("should have exactly 1 show", 1, shows.size)
        assertEquals("should only have emitted once", 0, showsQueue.size)
    }

    private fun observeShows(flowable: Flowable<List<ShowEntity>>) =
        flowable.skipWhile { it.isNullOrEmpty() }
            .subscribe { shows ->
                showsQueue.add(shows)
            }
}