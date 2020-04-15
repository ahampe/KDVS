package fho.kdvs.modules.subscription

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.database.joins.ShowTimeslotsJoin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val showDao: ShowDao
) : BaseRepository() {

    fun subscriptionByShowId(showId: Int): LiveData<SubscriptionEntity> {
        return subscriptionDao.getByShowId(showId)
    }

    fun subscriptionsForShows(shows: List<ShowEntity>): List<LiveData<SubscriptionEntity>> {
        val subscriptions = mutableListOf<LiveData<SubscriptionEntity>>()

        shows.forEach {
            subscriptions.add(subscriptionByShowId(it.id))
        }

        return subscriptions
    }

    fun subscribedShows(): List<ShowTimeslotsJoin> {
        val showsWithTimeslots = mutableListOf<ShowTimeslotsJoin>()
        val subscriptions = subscriptionDao.getAll()

        subscriptions.forEach {
            showDao.getShowTimeslotJoinsById(it.showId)?.let { s ->
                showsWithTimeslots.add(s)
            }
        }

        return showsWithTimeslots
    }
}