package fho.kdvs.subscription

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.SubscriptionDao
import fho.kdvs.global.database.SubscriptionEntity
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

    fun subscribedShows(): List<ShowEntity> {
        val shows = mutableListOf<ShowEntity>()
        val subscriptions = subscriptionDao.getAll()

        subscriptions.forEach {
            val show = showDao.getShowById(it.showId)
            show?.let { s ->
                shows.add(s)
            }
        }

        return shows
    }
}