package fho.kdvs.favorite

import androidx.lifecycle.LiveData
import fho.kdvs.global.BaseRepository
import fho.kdvs.global.database.FavoriteDao
import fho.kdvs.global.database.FavoriteEntity
import fho.kdvs.global.database.TrackEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) : BaseRepository() {

    private fun favoriteByTrackId(trackId: Int): LiveData<FavoriteEntity> {
        return favoriteDao.getByTrackId(trackId)
    }

    fun favoritesForTracks(tracks: List<TrackEntity>): List<LiveData<FavoriteEntity>> {
        val favorites = mutableListOf<LiveData<FavoriteEntity>>()
        tracks.forEach {
            favorites.add(favoriteByTrackId(it.trackId))
        }
        return favorites
    }
}