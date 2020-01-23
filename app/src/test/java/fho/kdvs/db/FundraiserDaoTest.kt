package fho.kdvs.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import fho.kdvs.MockObjects
import fho.kdvs.global.database.FundraiserEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FundraiserDaoTest : DatabaseTest() {
    @Test
    fun insert_basic() {
        val fundraiser = MockObjects.fundraiser
        insertFundraiser(fundraiser)

        val fundraiserDb = db.fundraiserDao().getAll()

        assert(fundraiserDb.contains(fundraiser))
        assertEquals(1, fundraiserDb.size)
    }

    @Test
    fun delete_fundraiser() {
        val fundraiser = MockObjects.fundraiser
        insertFundraiser(fundraiser)

        db.fundraiserDao().deleteAll()

        assertEquals("fundraiser delete failed", 0, db.fundraiserDao().getAll().size)
    }

    private fun insertFundraiser(fundraiser: FundraiserEntity) {
        db.fundraiserDao().insert(fundraiser)
    }
}