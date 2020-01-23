package fho.kdvs.db

import android.database.sqlite.SQLiteConstraintException
import fho.kdvs.MockObjects
import fho.kdvs.global.database.SubscriptionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SubscriptionDaoTest : DatabaseTest() {
    private val subscriptionDao by lazy { db.subscriptionDao() }
    private val showDao by lazy { db.showDao() }

    @Test
    fun insert_basic_noShow() {
        try {
            db.subscriptionDao().insert(SubscriptionEntity(showId = 0))
            throw AssertionError("Shouldn't be able to insert a subscription without a show first!")
        } catch (e: SQLiteConstraintException) {
        }
    }

    @Test
    fun insert_basic() {
        val (show) = MockObjects.showsWithOneTimeslot.first()
        val subscription = SubscriptionEntity(1, show.id)

        showDao.insert(show)
        subscriptionDao.insert(subscription)

        val subscriptionDb = db.subscriptionDao().getAll()

        assertTrue(subscriptionDb.contains(subscription))
        assertEquals(1, subscriptionDb.size)
    }

    @Test
    fun insert_multiple() {
        val showsWithTimeslots = MockObjects.showsWithMultipleTimeslots
        val subscriptions = mutableListOf<SubscriptionEntity>()

        showsWithTimeslots.map { s -> s.first }.forEachIndexed { i, show ->
            val subscription = SubscriptionEntity(i + 1, show.id)

            showDao.insert(show)
            subscriptionDao.insert(subscription)

            subscriptions.add(subscription)
        }

        val subscriptionDb = db.subscriptionDao().getAll()

        assertEquals(subscriptions.size, subscriptionDb.size)

        subscriptions.forEach { subscription ->
            assertTrue(subscriptionDb.contains(subscription))
        }
    }

    @Test
    fun delete_subscription_by_show() {
        val (show) = MockObjects.showsWithOneTimeslot.first()
        val subscription = SubscriptionEntity(showId = show.id)

        showDao.insert(show)
        subscriptionDao.insert(subscription)
        subscriptionDao.deleteByShowId(show.id)

        val subscriptionDb = db.subscriptionDao().getAll()

        assertEquals("delete subscription failed", 0, subscriptionDb.size)
    }
}