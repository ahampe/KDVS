package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TimeslotEntity

/**
 * Captures the one-to-many relation between [ShowEntity]s and their associated [TimeslotEntity]s.
 * Use in cases in which one needs to refer to all timeslots of a show.
 * (e.g. 'Democracy Now on Monday-Friday 3PM')
 *
 * [ShowTimeslotEntity] captures a specific show and timeslot relation.
 * */
class ShowTimeslotsJoin: Join() {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId", entity = TimeslotEntity::class)
    var timeslots: List<TimeslotEntity> = ArrayList()

    override fun equals(other: Any?): Boolean {
        val otherJoin = other as? ShowTimeslotsJoin
        return this.show == otherJoin?.show
                && this.timeslots == otherJoin?.timeslots
    }
}