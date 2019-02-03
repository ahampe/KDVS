package fho.kdvs.db

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fho.kdvs.DbTestUtils
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackDaoTest : DatabaseTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insert_basic_noBroadcast() {
        val track = DbTestUtils.createTracks().first()
        try {
            db.trackDao().insert(track)
            throw AssertionError("Shouldn't be able to insert a track without a broadcast first!")
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun insert_basic() {
        insertShow()
        insertBroadcast()

        val track = DbTestUtils.createTracks().first()
        db.trackDao().insert(track)

        val trackDb = db.trackDao().getAll()
        assert(trackDb.contains(track))
        assertEquals(1, trackDb.size)
    }

    @Test
    fun insert_multiple() {
        insertShow()
        insertBroadcast()

        val tracks = DbTestUtils.createTracks()
        tracks.forEach {
            db.trackDao().insert(it)
        }

        val tracksDb = db.trackDao().getAll()
        assertEquals(tracks.size, tracksDb.size)
        tracks.forEach {
            assert(tracksDb.contains(it))
        }
    }


    @Test
    fun select_by_show() {
        insertShow()
        insertBroadcast()
        insert_multiple()

        val tracks = DbTestUtils.createTracks()
            .filter { !it.airbreak }
        val tracksDb = db.trackDao().getTracksByShow(1888)

        assertEquals(tracks.size, tracksDb.size)
        tracks.forEach {
            assert(tracksDb.contains(it))
        }
    }

    @Test
    fun select_by_artist() {
        insertShow()
        insertBroadcast()
        insert_multiple()

        val tracks = DbTestUtils.createTracks().filter { it.artist == "Dolly Parton" }
        val tracksDb = db.trackDao().getTracksByArtist("Dolly Parton")

        assertEquals(tracks.size, tracksDb.size)
        tracks.forEach {
            assert(tracksDb.contains(it))
        }
    }

    @Test
    fun select_by_album() {
        insertShow()
        insertBroadcast()
        insert_multiple()

        val tracks = DbTestUtils.createTracks().filter { it.album == "Blue Smoke" }
        val tracksDb = db.trackDao().getTracksByAlbum("Blue Smoke")

        assertEquals(tracks.size, tracksDb.size)
        tracks.forEach {
            assert(tracksDb.contains(it))
        }
    }

    @Test
    fun select_by_artistalbum() {
        insertShow()
        insertBroadcast()
        insert_multiple()

        val tracks = DbTestUtils.createTracks().filter { it.artist == "Dolly Parton" && it.album == "Blue Smoke" }
        val tracksDb = db.trackDao().getTracksByArtistAlbum("Dolly Parton", "Blue Smoke")

        assertEquals(tracks.size, tracksDb.size)
        tracks.forEach {
            assert(tracksDb.contains(it))
        }
    }

    @Test
    fun select_by_label() {
        insertShow()
        insertBroadcast()
        insert_multiple()

        val tracks = DbTestUtils.createTracks().filter { it.label == "Waterbug" }
        val tracksDb = db.trackDao().getTracksByLabel("Waterbug")

        assertEquals(tracks.size, tracksDb.size)
        tracks.forEach {
            assert(tracksDb.contains(it))
        }
    }

    @Test
    fun delete_tracks_by_broadcast() {
        insertShow()
        insertBroadcast()
        insert_basic()

        val trackDb = db.trackDao().getAll().firstOrNull()

        try {
            db.trackDao().deleteByBroadcast(trackDb?.broadcastId)
        } catch (e: SQLiteConstraintException) {
        }

        assertEquals(0, db.trackDao().getTracksForBroadcast(trackDb?.broadcastId).size)
    }

    // Helper functions
    private fun insertShow() {
        val show = DbTestUtils.createShows().first()
        db.showDao().insert(show)
    }

    private fun insertBroadcast() {
        val broadcast = DbTestUtils.createBroadcasts().first()
        db.broadcastDao().insert(broadcast)
    }
}