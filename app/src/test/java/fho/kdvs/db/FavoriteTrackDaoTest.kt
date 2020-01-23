package fho.kdvs.db

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import fho.kdvs.MockObjects
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteTrackDaoTest : DatabaseTest() {
    @Test
    fun insert_basic_noTrack() {
        val favoriteTrack = MockObjects.favoriteTracks.first()
        try {
            db.favoriteTrackDao().insert(favoriteTrack)
            throw AssertionError("Shouldn't be able to insert a favoriteTrack without a track first!")
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun insert_basic() {
        insertShow()
        insertBroadcasts()
        insertTrack()

        val favoriteTrack = MockObjects.favoriteTracks.first()
        db.favoriteTrackDao().insert(favoriteTrack)

        val favoriteTrackDb = db.favoriteTrackDao().getAllFavoriteTracks()
        assert(favoriteTrackDb.contains(favoriteTrack))
        assertEquals(1, favoriteTrackDb.size)
    }

    @Test
    fun insert_multiple() {
        insertShow()
        insertBroadcasts()
        insertTracks()

        val favoriteTracks = MockObjects.favoriteTracks
        favoriteTracks.forEach {
            db.favoriteTrackDao().insert(it)
        }

        val favoriteTrackDb = db.favoriteTrackDao().getAllFavoriteTracks()
        favoriteTracks.forEach {
            assert(favoriteTrackDb.contains(it))
        }
        assertEquals(favoriteTracks.size, favoriteTrackDb.size)
    }


    @Test
    fun select_by_track() {
        insertShow()
        insertBroadcasts()
        insertTracks()
        insert_multiple()

        val favoriteTracks = MockObjects.favoriteTracks
        val favoriteTrackDb = db.favoriteTrackDao().getAllFavoriteTracks()

        assertEquals(favoriteTracks.size, favoriteTrackDb.size)
        favoriteTracks.forEach {
            assert(favoriteTrackDb.contains(it))
        }
    }

    @Test
    fun delete_by_track() {
        insertShow()
        insertBroadcasts()
        insertTrack()
        insert_basic()

        val trackDb = db.trackDao().getAll().first()

        try {
            db.favoriteTrackDao().deleteByTrackId(trackDb.trackId)
        } catch (e: SQLiteConstraintException) {
        }

        assertEquals(0, db.favoriteTrackDao().getAllFavoriteTracks()
            .count { b -> b.trackId == trackDb.trackId })
    }

    // Helper functions
    private fun insertShow() {
        val (show) = MockObjects.showsWithOneTimeslot.first()
        db.showDao().insert(show)
    }

    private fun insertBroadcasts() {
        MockObjects.broadcastsWithDetails.forEach {
            db.broadcastDao().insert(it)
        }
    }

    private fun insertTrack() {
        db.trackDao().insert(MockObjects.tracks.first())
    }

    private fun insertTracks() {
        MockObjects.tracks.forEach {
            db.trackDao().insert(it)
        }
    }
}