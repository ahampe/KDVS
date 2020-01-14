package fho.kdvs.favorite.track

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.extensions.toLiveData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteTrackRepository @Inject constructor(
    private val favoriteBroadcastDao: FavoriteBroadcastDao,
    private val favoriteTrackDao: FavoriteTrackDao
) : BaseRepository() {
    fun allShowBroadcastFavoriteJoins(): LiveData<List<ShowBroadcastFavoriteJoin>> {
        return favoriteBroadcastDao.allTimeslotShowBroadcastFavoriteJoins()
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }

    fun allShowBroadcastTrackFavoriteJoins(): LiveData<List<ShowBroadcastTrackFavoriteJoin>> {
        return favoriteTrackDao.allShowBroadcastTrackFavoriteJoins()
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }

    fun favoriteByTrackId(trackId: Int): LiveData<FavoriteTrackEntity> {
        return favoriteTrackDao.getByTrackId(trackId)
    }

    fun allFavoritesByBroadcast(broadcastId: Int): LiveData<List<FavoriteTrackEntity>> {
        return favoriteTrackDao.allFavoritesByBroadcast(broadcastId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }
}