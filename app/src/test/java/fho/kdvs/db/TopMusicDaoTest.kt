package fho.kdvs.db

import fho.kdvs.MockObjects
import fho.kdvs.topmusic.TopMusicType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TopMusicDaoTest : DatabaseTest() {
    private val topMusicDao by lazy { db.topMusicDao() }

    @Test
    fun insert_basic() {
        val topAdd = MockObjects.topAdds.first()

        topMusicDao.insert(topAdd)

        val topMusicDb = db.topMusicDao().getAllTopMusic()

        assertTrue(topMusicDb.contains(topAdd))
        assertEquals(1, topMusicDb.size)
    }

    @Test
    fun insert_multiple() {
        val topAdds = MockObjects.topAdds

        topAdds.forEach {
            topMusicDao.insert(it)
        }

        val topMusicDb = db.topMusicDao().getAllTopMusic()

        topAdds.forEach {
            assertTrue(topMusicDb.contains(it))
        }

        assertEquals(topAdds.size, topMusicDb.size)
    }

    @Test
    fun get_topMusic_of_type_for_week() {
        val topAdds = MockObjects.topAdds

        topAdds.forEach {
            topMusicDao.insert(it)
        }

        val week = topAdds.first().weekOf
        val topAddsForSpecificWeek = topAdds.filter { t -> t.weekOf == week }
        val topMusicDb = db.topMusicDao().topMusicForWeekOfType(week, TopMusicType.ADD)

        topAddsForSpecificWeek.forEach {
            assertTrue(topMusicDb.contains(it))
        }

        assertEquals(topAddsForSpecificWeek.size, topMusicDb.size)
    }

    @Test
    fun update_topMusic_metadata() {
        val topAdd = MockObjects.topAdds.first()

        topMusicDao.insert(topAdd)
        topMusicDao.updateTopMusicAlbum(topAdd.topMusicId, "test")
        topAdd.album = "test"

        val topMusicDb = db.topMusicDao().getAllTopMusic().filter {
            it.album == "test"
        }

        assertTrue(topMusicDb.contains(topAdd))
        assertEquals(1, topMusicDb.size)
    }
}