package fho.kdvs.schedule

import fho.kdvs.global.database.ShowEntity
import org.threeten.bp.OffsetDateTime

/** Shows on the KDVS programming grid may share a single span of time each week and alternate.
 * This construct is built for holding information about individual TimeSlots, which may contain one or more shows. */
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