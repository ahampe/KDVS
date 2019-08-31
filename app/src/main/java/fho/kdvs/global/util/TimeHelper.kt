package fho.kdvs.global.util

import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.enums.Day
import fho.kdvs.schedule.TimeSlot
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.lang.Math.abs
import java.util.*
import kotlin.math.roundToInt

/**
 * Helper object that configures show times for the week.
 *
 * Because the schedule only provides the time and day of week, we define the week as the range
 * Sunday Jan 4, 1970 to Saturday Jan 10, 1970 for convenience.
 *
 * Note: UTC is always used for consistent storage of times.
 */
object TimeHelper {

    private val UTC_ID: ZoneId = ZoneId.of("UTC")
    private val UTC_OFFSET: ZoneOffset = ZoneOffset.UTC
    private val PACIFIC_ID: ZoneId = ZoneId.of( "America/Los_Angeles" )

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

    /** A 24-hour time formatter, used for displaying show times (which are in UTC) in the UI. */
    val showTimeFormatter24: DateTimeFormatter = DateTimeFormatter
        .ofPattern("hh:mm", Locale.US)
        .withZone(UTC_ID)

    // region Week Times (for Show entities)
    /** Offset in days from Jan 1 1970. Necessary because we want the week to begin on Sunday, Jan 4. */
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
        return makeEpochDay(time.dayOfWeek.value % 7)
            .plusHours(time.hour.toLong())
            .plusMinutes(time.minute.toLong())
            .plusSeconds(time.second.toLong())
    }

    /**
     * Converts time from one zone to another.
     */
    private fun convertZoneTime(convertFrom: ZoneId, convertTo: ZoneId, time: OffsetDateTime): OffsetDateTime {
        val convertFromZoneOffset = convertFrom.rules.getOffset(LocalDateTime.now())
        val convertToZoneOffset = convertTo.rules.getOffset(LocalDateTime.now())

        val secondsOffset = (convertToZoneOffset.totalSeconds - convertFromZoneOffset.totalSeconds).toLong()

        return time.plusSeconds(secondsOffset)
    }

    private fun getSystemTimeZone(): ZoneId {
        return ZoneId.of(TimeZone.getDefault().id)
    }

    /**
     * Creates a date corresponding to the next-occurring instance of a week-day absolute time
     * relative to user's system timezone, from an epoch time.
     *
     * E.g. If current system time is absolute Fri, Jan 5 4PM and passed-in value corresponds to
     * epoch Fri 3PM, return value will correspond to absolute Fri, Jan 12 3PM.
     */
    fun makeRealWeekRelativeTimeFromEpochTime(epochTime: OffsetDateTime): OffsetDateTime? {
        val now = OffsetDateTime.now()

        val offset = getSystemTimeZone().rules.getOffset(LocalDateTime.now())

        // TODO: change this line after implementing real-time dynamic kdvs times based on system timezone
        val adjustedEpochTime = convertZoneTime(PACIFIC_ID, getSystemTimeZone(), epochTime)

        when {
            now.dayOfWeek < adjustedEpochTime.dayOfWeek -> {
                val nowPlusDays = now.plusDays((adjustedEpochTime.dayOfWeek.value - now.dayOfWeek.value).toLong())
                return OffsetDateTime.of(
                    nowPlusDays.year,
                    nowPlusDays.monthValue,
                    nowPlusDays.dayOfMonth,
                    adjustedEpochTime.hour,
                    adjustedEpochTime.minute,
                    adjustedEpochTime.second,
                    adjustedEpochTime.nano,
                    offset)
            }
            now.dayOfWeek > adjustedEpochTime.dayOfWeek -> {
                val nowPlusDays = now.plusDays(7 - (now.dayOfWeek.value - adjustedEpochTime.dayOfWeek.value).toLong())
                return OffsetDateTime.of(
                    nowPlusDays.year,
                    nowPlusDays.monthValue,
                    nowPlusDays.dayOfMonth,
                    adjustedEpochTime.hour,
                    adjustedEpochTime.minute,
                    adjustedEpochTime.second,
                    adjustedEpochTime.nano,
                    offset)
            }
            now.dayOfWeek == adjustedEpochTime.dayOfWeek -> {
                when {
                    now.toLocalTime() < adjustedEpochTime.toLocalTime() -> {
                        return OffsetDateTime.of(
                            now.year,
                            now.monthValue,
                            now.dayOfMonth,
                            adjustedEpochTime.hour,
                            adjustedEpochTime.minute,
                            adjustedEpochTime.second,
                            adjustedEpochTime.nano,
                            offset)
                    }
                    now.toLocalTime() > adjustedEpochTime.toLocalTime() -> {
                        val nowPlusDays = now.plusDays(7)
                        return OffsetDateTime.of(
                            nowPlusDays.year,
                            nowPlusDays.monthValue,
                            nowPlusDays.dayOfMonth,
                            adjustedEpochTime.hour,
                            adjustedEpochTime.minute,
                            adjustedEpochTime.second,
                            adjustedEpochTime.nano,
                            offset)
                    }
                    now.toLocalTime() == adjustedEpochTime.toLocalTime() -> {
                        val nowPlusDays = now.plusDays(7)
                        return OffsetDateTime.of(
                            nowPlusDays.year,
                            nowPlusDays.monthValue,
                            nowPlusDays.dayOfMonth,
                            adjustedEpochTime.hour,
                            adjustedEpochTime.minute,
                            adjustedEpochTime.second,
                            adjustedEpochTime.nano,
                            offset)
                    }
                }
            }
        }

        return null
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

        // Last show of week will have a timeEnd dayOfWeek < timeStart dayOfWeek, so we must make an exception for this
        val isEndOfWeek = (b.dayOfWeek.value % 7) < (a.dayOfWeek.value % 7)

        val midnight = if (isFirstHalfOrEntireSegment) {
            val nextDay = a.plusDays(1)
            OffsetDateTime.of(nextDay.year, nextDay.monthValue, nextDay.dayOfMonth, 0, 0,0,
                0, nextDay.offset)
        } else {
            OffsetDateTime.of(b.year, b.monthValue, b.dayOfMonth, 0, 0,0, 0, b.offset)
        }

        return if (isFirstHalfOrEntireSegment){
            val min = if (b < midnight && !isEndOfWeek) b else midnight
            (abs(ChronoUnit.MINUTES.between(min, a)) / 30).toInt()
        } else {
            (abs(ChronoUnit.MINUTES.between(b, midnight)) / 30).toInt()
        }
    }

    /** Returns current time in the zone associated with KDVS. */
    @JvmStatic
    fun getNow(): OffsetDateTime {
        return ZonedDateTime.now(this.PACIFIC_ID).toOffsetDateTime()
    }

    @JvmStatic
    fun getDurationInSecondsBetween(start: OffsetDateTime, end: OffsetDateTime): Double {
        return if (start.hour > end.hour) {
            ((((24 - start.hour) + end.hour) * 3600 + end.minute * 60 + end.second)
                    - (start.minute * 60 + start.second)).toDouble()
        } else {
            ((end.hour * 3600 + end.minute * 60 + end.second)
                    - (start.hour * 3600 + start.minute * 60 + start.second)).toDouble()
        }
    }

    @JvmStatic
    fun getPercentageInDurationRelativeToNow(start: OffsetDateTime, end: OffsetDateTime): Int {
        val now = getDurationInSecondsBetween(start, getNow())
        val duration = getDurationInSecondsBetween(start, end)

        if (duration == 0.0) return 0
        return if (duration < now) 0 else
            (100 * (now / duration)).roundToInt()
    }

    /** Returns true if current time falls within timeslot's time range. */
    @JvmStatic
    fun isTimeSlotForCurrentShow(timeslot: TimeSlot): Boolean {
        val scheduleTime = makeEpochRelativeTime(getNow())
        return (scheduleTime >= timeslot?.timeStart) && (scheduleTime < timeslot?.timeEnd)
    }

    /** Returns true if broadcast is currently live on-air. */
    @JvmStatic
    fun isShowBroadcastLive(show: ShowEntity, broadcast: BroadcastEntity): Boolean {
        val now = getNow()
        return broadcast.date!!.year == now.year &&
                broadcast.date!!.dayOfYear == now.dayOfYear &&
                (now.dayOfWeek == show.timeStart!!.dayOfWeek ||
                        now.dayOfWeek == show.timeEnd!!.dayOfWeek) &&
                now.hour >= show.timeStart!!.hour &&
                (now.hour < show.timeEnd!!.hour ||
                        (now.hour == show.timeEnd!!.hour && now.minute < show.timeEnd!!.minute) ||
                            now.dayOfWeek != show.timeEnd!!.dayOfWeek)
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
        Pair(makeEpochDay(day.ordinal), makeEpochDay(day.ordinal + 1))

    /** Creates a day that is [DAY_OFFSET] + [ordinal] days from epoch start. */
    private fun makeEpochDay(ordinal: Int) = OffsetDateTime
        .ofInstant(Instant.EPOCH, UTC_ID)
        .plusDays(DAY_OFFSET + ordinal.toLong())

    // endregion

    // region Local Dates (for Broadcast entities)
    fun getLocalNow(): LocalDate {
        return ZonedDateTime.now(this.PACIFIC_ID).toLocalDate()
    }

    /** Creates a date given a string in [dateFormatter]'s format. */
    fun makeLocalDate(ymd: String): LocalDate {
        return LocalDate.parse(ymd, dateFormatter)
    }

    fun makeLocalDate(y: String?, m: String?, d: String?): LocalDate {
        return makeLocalDate(
            "${y?.padStart(4, '0')}" +
                    "-${m?.padStart(2, '0')}" +
                    "-${d?.padStart(2, '0')}"
        )
    }
    // endregion

    // region String/Int Conversions
    fun monthStrToInt(m: String?): Int {
        return when (m?.toUpperCase()) {
            "JANUARY" -> 1
            "FEBRUARY" -> 2
            "MARCH" -> 3
            "APRIL" -> 4
            "MAY" -> 5
            "JUNE" -> 6
            "JULY" -> 7
            "AUGUST" -> 8
            "SEPTEMBER" -> 9
            "OCTOBER" -> 10
            "NOVEMBER" -> 11
            "DECEMBER" -> 12
            else -> 0
        }
    }

    fun monthIntToStr(m: Int?): String {
        return when (m) {
            1 -> "JANUARY"
            2 -> "FEBRUARY"
            3 -> "MARCH"
            4 -> "APRIL"
            5 -> "MAY"
            6 -> "JUNE"
            7 -> "JULY"
            8 -> "AUGUST"
            9 -> "SEPTEMBER"
            10 -> "OCTOBER"
            11 -> "NOVEMBER"
            12 -> "DECEMBER"
            else -> ""
        }
    }
    // endregion
}