package fho.kdvs.db

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fho.kdvs.DbTestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BroadcastDaoTest : DatabaseTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insert_basic_noShow() {
        val broadcast = DbTestUtils.createBroadcasts().first()
        try {
            db.broadcastDao().insert(broadcast)
            throw AssertionError("Shouldn't be able to insert a broadcast without a show first!")
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun insert_basic() {
        insertShow()
        val broadcast = DbTestUtils.createBroadcasts().first()
        db.broadcastDao().insert(broadcast)

        val broadcasts = db.broadcastDao().getAll()
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)
    }

    @Test
    fun insert_multiple() {
        insertShow()
        val broadcasts = DbTestUtils.createBroadcasts()
        broadcasts.forEach {
            db.broadcastDao().insert(it)
        }

        val broadcastsDb = db.broadcastDao().getAll()
        assert(broadcastsDb.size == broadcasts.size)
        broadcasts.forEach {
            assert(broadcastsDb.contains(it))
        }
    }

    @Test
    fun select_by_show() {
        insertShow()
        val broadcast = DbTestUtils.createBroadcasts().first()
        insert_basic()

        val broadcasts = db.broadcastDao().getBroadcastsForShow(1888)
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)
    }

    @Test
    fun select_by_artist() {
        insertShow()
        insert_basic()
        insertTracks()
        val broadcast = DbTestUtils.createBroadcasts().first()

        val broadcasts = db.broadcastDao().getBroadcastsByArtist("Dolly Parton")
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)
    }

    @Test
    fun select_by_album() {
        insertShow()
        insert_basic()
        insertTracks()
        val broadcast = DbTestUtils.createBroadcasts().first()

        val broadcasts = db.broadcastDao().getBroadcastsByAlbum("Blue Smoke")
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)
    }

    @Test
    fun select_by_artistalbum() {
        insertShow()
        insert_basic()
        insertTracks()
        val broadcast = DbTestUtils.createBroadcasts().first()

        val broadcasts = db.broadcastDao().getBroadcastsByArtistAlbum("Dolly Parton", "Blue Smoke")
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)
    }

    @Test
    fun select_by_label() {
        insertShow()
        insert_basic()
        insertTracks()
        val broadcast = DbTestUtils.createBroadcasts().first()

        val broadcasts = db.broadcastDao().getBroadcastsByLabel("Waterbug")
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)
    }

    @Test
    fun delete_broadcast() {
        insertShow()
        val broadcast = DbTestUtils.createBroadcasts().first()
        insert_basic()

        val broadcasts = db.broadcastDao().getAll()
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)

        try {
            db.broadcastDao().deleteBroadcast(broadcast.broadcastId)
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun delete_broadcasts_for_show() {
        insertShow()
        val broadcast = DbTestUtils.createBroadcasts().first()
        insert_basic()

        val broadcasts = db.broadcastDao().getAll()
        assert(broadcasts.contains(broadcast))
        assert(broadcasts.size == 1)

        try {
            db.broadcastDao().deleteBroadcastsForShow(broadcast.showId)
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun updateBroadcast() {
        insertShow()
        val broadcast = DbTestUtils.createBroadcasts().first()
        db.broadcastDao().insert(broadcast)

        val broadcasts = db.broadcastDao().getAll()
        assertTrue("Could not find broadcast: $broadcast", broadcasts.contains(broadcast))
        assertEquals("Should have 1 broadcast", broadcasts.size, 1)

        broadcast.descr = "Updated descr"
        broadcast.imageHref = "https://i.kym-cdn.com/photos/images/original/001/356/199/dd9.png"

        db.broadcastDao().updateBroadcast(broadcast.broadcastId, broadcast.descr, broadcast.imageHref)
        val updatedBroadcasts = db.broadcastDao().getAll()
        assertTrue("Could not find broadcast: $broadcast", updatedBroadcasts.contains(broadcast))
        assertEquals("Should have 1 broadcast", updatedBroadcasts.size, 1)
    }

    // Helper function that inserts a show that will serve as the parent of broadcasts in these tests
    private fun insertShow() {
        val show = DbTestUtils.createShows().first()
        db.showDao().insert(show)
    }

    private fun insertTracks() {
        val tracks = DbTestUtils.createTracks()
        tracks.forEach {
            db.trackDao().insert(it)
        }
    }
}