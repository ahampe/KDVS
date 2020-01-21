package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.global.database.FavoriteTrackEntity
import fho.kdvs.global.database.TrackEntity

class TrackFavoriteJoin {
    @Embedded
    var track: TrackEntity? = null

    @Relation(parentColumn = "trackId", entityColumn = "trackId")
    var favorite: List<FavoriteTrackEntity> = ArrayList()
}