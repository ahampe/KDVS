package fho.kdvs.global.util

import fho.kdvs.global.enums.Day
import fho.kdvs.schedule.TimeSlot
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.lang.Math.abs
import java.util.*

/**
 * Helper object that configures show times for the week.
 *
 * Because the schedule only provides the time and day of week, we define the week as the range
 * Sunday Jan 4, 1970 to Saturday Jan 10, 1970 for convenience.
 *
 * Note: UTC is always used for consistent storage of times.
 */
object TimeHelper {

    private val UTC_ID = ZoneId.of("UTC")
    private val UTC_OFFSET = ZoneOffset.UTC

    // TODO might not use
//    private val LOCAL_ID = ZoneId.of("America/Los_Angeles")
//    private val LOCAL_RULES = LOCAL_ID.rules

    /**
     * Formatter that will be used for parsing broadcast datetimes.
     * Uses local date time pattern, e.g., '2011-12-03'
     */
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /** For displaying broadcast LocalDates in the UI. */
    val uiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    /** A 12-hour time formatter, used for displaying show times (which are in UTC) in the UI. */
    val showTimeFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("hh:mm a", Locale.US)
        .withZone(UTC_ID)

    // TODO maybe delete
    /** Formatter that will be used throughout the app for parsing show times with the day of month. */
    private val dayTimeFormatter = DateTimeFormatter
        .ofPattern("d HH:mm", Locale.US)
        .withZone(UTC_ID)

    // region Week Times (for Show entities)
    /** Offset in days from Jan 1 1970. Necessary because we want the week to begin on Sunday. */
    private const val DAY_OFFSET = 3

    // private helpers for converting between 12 and 24 hour times
    private val time12h = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val time24h = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    /**
     * Creates a date given a 12-hour time string (e.g. "3:00 PM") and a [Day] of week.
     * The date generated with this method will fall within the week defined in [TimeHelper].
     */
    fun makeWeekTime12h(time: String, day: Day): OffsetDateTime {
        val timeConverted = LocalTime.parse(time, time12h).format(time24h)
        return makeWeekTime24h(timeConverted, day)
    }

    /**
     * Creates a date given a 24-hour time string (e.g. "13:00") and a [Day] of week.
     * The date generated with this method will fall within the week defined in [TimeHelper].
     */
    fun makeWeekTime24h(time: String, day: Day): OffsetDateTime {
        val paddedTime = time.padStart(5, '0')
        val dayOfMonth = (day.ordinal + DAY_OFFSET + 1).toString().padStart(2, '0')
        return LocalDateTime.parse("1970-01-${dayOfMonth}T$paddedTime", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .atOffset(UTC_OFFSET)
    }

    /**
     * Creates a date relative to epoch with an [OffsetDateTime] variable
     * The date generated with this method will fall within the week defined in [TimeHelper].
     */
    fun makeEpochRelativeTime(time: OffsetDateTime): OffsetDateTime {
        return makeDay(time.dayOfWeek.value)
            .plusHours(time.hour.toLong())
            .plusMinutes(time.minute.toLong())
            .plusSeconds(time.second.toLong())
    }

    /**
     * Creates a date given the hour and minute as Ints as well as a [Day].
     * The date generated with this method will fall within the week defined in [TimeHelper].
     */
    fun makeWeekTime(hour: Int, minute: Int, day: Day): OffsetDateTime {
        return makeWeekTime24h("$hour:$minute", day)
    }

    fun getTimeDifferenceInMs(a: OffsetDateTime, b: OffsetDateTime) : Long {
        return abs(ChronoUnit.SECONDS.between(b, a)) * 1000
    }

    /**
     * Gets difference in 30-min intervals between two show times.
     * Only counts time before midnight for the first half of a time interval spread across two days, and
     * the time after midnight for the second half.
     * Assumes a show can be across at most two days.
     * Assumes b > a.
     */
    @JvmStatic
    fun getTimeDifferenceInHalfHoursPerDay(a: OffsetDateTime, b: OffsetDateTime, timeslot: TimeSlot) : Int {
        val isFirstHalfOrEntireSegment = timeslot.isFirstHalfOrEntireSegment

        val midnight = if (isFirstHalfOrEntireSegment) {
            val nextDay = a.plusDays(1)
            OffsetDateTime.of(nextDay.year, nextDay.monthValue, nextDay.dayOfMonth, 0, 0,0,
                0, nextDay.offset)
        } else {
            OffsetDateTime.of(b.year, b.monthValue, b.dayOfMonth, 0, 0,0, 0, b.offset)
        }

        return if (isFirstHalfOrEntireSegment){
            val min = if (b < midnight) b else midnight
            (abs(ChronoUnit.MINUTES.between(min, a)) / 30).toInt()
        } else {
            (abs(ChronoUnit.MINUTES.between(b, midnight)) / 30).toInt()
        }
    }

    /**
     * When scraping the schedule, we only encounter a show on its starting day.
     * This function will correct the end date to the correct day.
     * This should only be used for dates in the week range defined in [TimeHelper].
     */
    fun addDay(date: OffsetDateTime): OffsetDateTime {
        val addedDay = if (date.dayOfWeek != DayOfWeek.SATURDAY) 1L else -6L
        return date.plusDays(addedDay)
    }

    /** Constructs a date range for a given [Day]. Note the start and end day of month:
     * For [Day.SUNDAY], will return (00:00 Sun Jan 4 1970, 00:00 Mon Jan 5 1970) in UTC.
     * For [Day.SATURDAY], will return (00:00 Sat Jan 10 1970, 00:00 Sun Jan 11 1970) in UTC. */
    fun makeDayRange(day: Day): Pair<OffsetDateTime, OffsetDateTime> =
        Pair(makeDay(day.ordinal), makeDay(day.ordinal + 1))

    /** Creates a day that is [DAY_OFFSET] + [ordinal] days from epoch start. */
    private fun makeDay(ordinal: Int) = OffsetDateTime
        .ofInstant(Instant.EPOCH, UTC_ID)
        .plusDays(DAY_OFFSET + ordinal.toLong())

    // endregion

    // region Local Dates (for Broadcast entities)
    /** Creates a date given a string in [dateFormatter]'s format. */
    fun makeLocalDate(ymd: String): LocalDate {
        return LocalDate.parse(ymd, dateFormatter)
    }

    fun makeLocalDate(y: String?, m: String?, d: String?): LocalDate {
        return TimeHelper.makeLocalDate(
            "${y?.padStart(4, '0')}" +
                    "-${m?.padStart(2, '0')}" +
                    "-${d?.padStart(2, '0')}"
        )
    }
    // endregion

    // TODO arbitrary time range (may not need)
}