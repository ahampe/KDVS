package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.TrackEntity

class BroadcastTrackFavoriteJoin {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(
        parentColumn = "broadcastId",
        entityColumn = "broadcastId",
        entity = TrackEntity::class
    )
    var trackFavorite: List<TrackFavoriteJoin> = ArrayList()
}