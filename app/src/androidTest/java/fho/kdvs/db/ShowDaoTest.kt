package fho.kdvs.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fho.kdvs.DbTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowDaoTest : DatabaseTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insert_basic() {
        val show = DbTestUtils.createShow()
        db.showDao().insert(show)

        val shows = db.showDao().getAll()
        assert(shows.contains(show))
        assert(shows.size == 1)
    }
}