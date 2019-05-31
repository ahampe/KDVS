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
    fun allShowBroadcastTrackFavoriteJoins(): LiveData<List<ShowBroadcastTrackFavoriteJoin>> {
        return favoriteDao.allFavoritedTracks()
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }

    fun favoriteByTrackId(trackId: Int): LiveData<FavoriteEntity> {
        return favoriteDao.getByTrackId(trackId)
    }

    fun allFavoritesByBroadcast(broadcastId: Int): LiveData<List<FavoriteEntity>> {
        return favoriteDao.allFavoritesByBroadcast(broadcastId)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()
    }
}