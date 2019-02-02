package fho.kdvs.model

enum class Quarter(val value: Int) {
    WINTER(0),
    SPRING(1),
    SUMMER(2),
    FALL(3);

    companion object {
        private val map = Quarter.values().associateBy(Quarter::value)
        fun fromInt(value: Int) = map[value]
    }
}