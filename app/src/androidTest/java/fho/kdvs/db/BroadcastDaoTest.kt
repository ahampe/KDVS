package fho.kdvs.db

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fho.kdvs.DbTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BroadcastDaoTest : DatabaseTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insert_basic_noShow() {
        val broadcast = DbTestUtils.createBroadcast()
        try {
            db.broadcastDao().insert(broadcast)
            throw AssertionError("Shouldn't be able to insert a broadcast without a show first!")
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun insert_basic() {
        insertShow()
        val broadcast = DbTestUtils.createBroadcast()
        db.broadcastDao().insert(broadcast)

        val broadcasts = db.broadcastDao().getAll()
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)
    }

    // Helper function that inserts a show that will serve as the parent of broadcasts in these tests
    private fun insertShow() {
        val show = DbTestUtils.createShow()
        db.showDao().insert(show)
    }
}