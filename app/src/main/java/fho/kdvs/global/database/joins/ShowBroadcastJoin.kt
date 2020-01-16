package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity

class ShowBroadcastJoin {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId")
    var broadcast: List<BroadcastEntity> = ArrayList()
}