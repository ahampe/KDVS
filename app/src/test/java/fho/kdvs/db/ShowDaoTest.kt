package fho.kdvs.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fho.kdvs.DbTestUtils
import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.util.TimeHelper
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

        val shows = showDao.getAll()
        assertTrue(shows.contains(show))
        assertEquals(1, shows.size)
    }

    @Test
    fun get_all_shows_for_time_range() {
        val shows = DbTestUtils.createShows()
        shows.forEach {
            showDao.insert(it)
        }

        val timeStart = TimeHelper.makeWeekTime24h("00:00", Day.SUNDAY)
        val timeEnd = TimeHelper.makeWeekTime24h("03:00", Day.SUNDAY)

        val showsDb = showDao.getShowsInTimeRange(timeStart, timeEnd, Quarter.SPRING, 1943)

        assertEquals(shows.size, showsDb.size)
        shows.forEach {
            assert(showsDb.contains(it))
        }
    }

    @Test
    fun observable_insert() {
        compositeDisposable += observeShows(showDao.allShows())

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