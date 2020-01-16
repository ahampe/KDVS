package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.TrackEntity

class BroadcastTrackJoin {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(parentColumn = "broadcastId", entityColumn = "broadcastId")
    var track: List<TrackEntity> = ArrayList()
}