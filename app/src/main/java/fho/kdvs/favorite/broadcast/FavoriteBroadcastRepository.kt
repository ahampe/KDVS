package fho.kdvs.favorite.broadcast

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.extensions.toLiveData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteBroadcastRepository @Inject constructor(
    private val favoriteBroadcastDao: FavoriteBroadcastDao
) : BaseRepository() {
    fun allShowBroadcastFavoriteJoins(): LiveData<List<ShowBroadcastFavoriteJoin>> {
        return favoriteBroadcastDao.allShowBroadcastFavoriteJoins()
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }

    fun favoriteByBroadcastId(broadcastId: Int): LiveData<FavoriteBroadcastEntity> {
        return favoriteBroadcastDao.getByBroadcastId(broadcastId)
    }

    fun allFavoritesByBroadcast(broadcastId: Int): LiveData<List<FavoriteBroadcastEntity>> {
        return favoriteBroadcastDao.allFavoritesByBroadcast(broadcastId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }
}