package fho.kdvs.db

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fho.kdvs.MockObjects
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteBroadcastDaoTest : DatabaseTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insert_basic_noBroadcast() {
        val favoriteBroadcast = MockObjects.favoriteBroadcasts.first()
        try {
            db.favoriteBroadcastDao().insert(favoriteBroadcast)
            throw AssertionError("Shouldn't be able to insert a favoriteBroadcast without a broadcast first!")
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun insert_basic() {
        insertShow()
        insertBroadcast()

        val favoriteBroadcast = MockObjects.favoriteBroadcasts.first()
        db.favoriteBroadcastDao().insert(favoriteBroadcast)

        val favoriteBroadcastDb = db.favoriteBroadcastDao().getAllFavoriteBroadcasts()
        assert(favoriteBroadcastDb.contains(favoriteBroadcast))
        assertEquals(1, favoriteBroadcastDb.size)
    }

    @Test
    fun insert_multiple() {
        insertShow()
        insertBroadcasts()

        val favoriteBroadcasts = MockObjects.favoriteBroadcasts
        favoriteBroadcasts.forEach {
            db.favoriteBroadcastDao().insert(it)
        }

        val favoriteBroadcastDb = db.favoriteBroadcastDao().getAllFavoriteBroadcasts()
        favoriteBroadcasts.forEach {
            assert(favoriteBroadcastDb.contains(it))
        }
        assertEquals(favoriteBroadcasts.size, favoriteBroadcastDb.size)
    }


    @Test
    fun select_by_broadcast() {
        insertShow()
        insertBroadcasts()
        insert_multiple()

        val favoriteBroadcasts = MockObjects.favoriteBroadcasts
        val favoriteBroadcastDb = db.favoriteBroadcastDao().getAllFavoriteBroadcasts()

        assertEquals(favoriteBroadcasts.size, favoriteBroadcastDb.size)
        favoriteBroadcasts.forEach {
            assert(favoriteBroadcastDb.contains(it))
        }
    }

    @Test
    fun delete_by_broadcast() {
        insertShow()
        insertBroadcast()
        insert_basic()

        val broadcastDb = db.broadcastDao().getAll().first()

        try {
            db.favoriteBroadcastDao().deleteByBroadcastId(broadcastDb.broadcastId)
        } catch (e: SQLiteConstraintException) {
        }

        assertEquals(0, db.favoriteBroadcastDao().getAllFavoriteBroadcasts()
            .count { b -> b.broadcastId == broadcastDb.broadcastId})
    }

    // Helper functions
    private fun insertShow() {
        val (show) = MockObjects.showsWithOneTimeslot.first()
        db.showDao().insert(show)
    }

    private fun insertBroadcast() {
        db.broadcastDao().insert(MockObjects.broadcastsWithDetails.first())
    }

    private fun insertBroadcasts() {
        MockObjects.broadcastsWithDetails.forEach {
            db.broadcastDao().insert(it)
        }
    }
}