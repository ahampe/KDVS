package fho.kdvs.global.database

import fho.kdvs.global.enums.Quarter
import org.threeten.bp.OffsetDateTime

/**
 * POJO for [ShowEntity] and [TimeslotEntity] one-to-one relation.
 * Use in cases in which one needs to refer to a specific [TimeslotEntity] for a [ShowEntity].
 * (e.g. 'Democracy Now on Tuesday 3PM')
 *
 * [ShowTimeslotsJoin] captures the one-to-many relation.
 * */
data class ShowTimeslotEntity (
    override val id: Int,
    override var name: String? = null,
    override var host: String? = null,
    override var genre: String? = null,
    override var defaultDesc: String? = null,
    override var defaultImageHref: String? = null,
    override var quarter: Quarter? = null,
    override var year: Int? = null,
    var timeStart: OffsetDateTime? = null,
    var timeEnd: OffsetDateTime? = null
): Show()

fun makeShowTimeslot(show: ShowEntity, timeslot: TimeslotEntity) = ShowTimeslotEntity(
    id = show.id,
    name = show.name,
    host = show.host,
    genre = show.genre,
    defaultDesc = show.defaultDesc,
    defaultImageHref = show.defaultImageHref,
    quarter = show.quarter,
    year = show.year,
    timeStart = timeslot.timeStart,
    timeEnd = timeslot.timeEnd
)