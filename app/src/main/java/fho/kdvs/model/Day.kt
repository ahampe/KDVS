package fho.kdvs.model

enum class Day(val value: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    companion object {
        private val map = Day.values().associateBy(Day::value)
        fun fromInt(value: Int) = map[value]
        fun getNextDay(day: Day): Day = fromInt((day.value +1) % 7)!!
    }
}