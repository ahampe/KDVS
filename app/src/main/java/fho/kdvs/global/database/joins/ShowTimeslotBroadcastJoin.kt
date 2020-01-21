package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowTimeslotEntity

/** Captures relation between a [BroadcastEntity] and the [ShowTimeslotEntity] to which it pertains .*/
class ShowTimeslotBroadcastJoin {
    @Embedded
    var show: ShowTimeslotEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId")
    var broadcast: List<BroadcastEntity> = ArrayList()
}