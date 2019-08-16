package fho.kdvs.global.extensions

import fho.kdvs.global.util.TimeHelper
import org.threeten.bp.OffsetDateTime

/**
 * Takes in a time relative to KDVS (i.e. Pacific) and returns the time relative to system's timezone.
 * Should be called in any instance in which we're displaying time information to the user.
 */
fun OffsetDateTime.fromPacific(): OffsetDateTime {
    return TimeHelper.convertZoneTime(
        TimeHelper.PACIFIC_ID,
        TimeHelper.getSystemTimeZone(),
        this
    )
}