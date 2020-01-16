package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.FavoriteBroadcastEntity

class BroadcastFavoriteJoin: Join() {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(parentColumn = "broadcastId", entityColumn = "broadcastId")
    var favorite: List<FavoriteBroadcastEntity> = ArrayList()
}