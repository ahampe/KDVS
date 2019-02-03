package fho.kdvs.schedule

import fho.kdvs.global.database.ShowEntity
import org.threeten.bp.OffsetDateTime

data class TimeSlot(
    val timeStart: OffsetDateTime?,
    val timeEnd: OffsetDateTime?,
    val imageHref: String?,
    val ids: List<Int>,
    val names: List<String?>
) {
    constructor(shows: List<ShowEntity>) : this(
        shows.first().timeStart,
        shows.first().timeEnd,
        shows.first().defaultImageHref,
        shows.map { it.id },
        shows.map { it.name }
    )
}