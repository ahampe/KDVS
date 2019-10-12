package fho.kdvs.favorite

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.FavoriteDao
import fho.kdvs.global.database.FavoriteEntity
import fho.kdvs.global.database.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.extensions.toLiveData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) : BaseRepository() {
    fun insert(favorite: FavoriteEntity) = favoriteDao.insert(favorite)

    fun allShowBroadcastTrackFavoriteJoins(): LiveData<List<ShowBroadcastTrackFavoriteJoin>> {
        return favoriteDao.allShowBroadcastTrackFavoriteJoins()
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }

    fun deleteByTrackId(trackId: Int) = favoriteDao.deleteByTrackId(trackId)

    fun allFavoritesByBroadcast(broadcastId: Int): LiveData<List<FavoriteEntity>> {
        return favoriteDao.allFavoritesByBroadcast(broadcastId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }
}