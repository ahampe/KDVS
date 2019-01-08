package fho.kdvs.database.models

import fho.kdvs.database.entities.ShowEntity
import java.util.*

data class Show(
    val id: Int?,
    val name: String?,
    var host: String?,
    var genre: String?,
    var defaultDesc: String?,
    var defaultImageHref: String?,
    var timeStart: Date?,
    var timeEnd: Date?,
    var dayOfWeek: String?,
    var quarter: String?,
    var year: Int?
) {
    constructor(showEntity: ShowEntity) : this(
        showEntity.id,
        showEntity.name,
        showEntity.host,
        showEntity.genre,
        showEntity.defaultDesc,
        showEntity.defaultImageHref,
        showEntity.timeStart,
        showEntity.timeEnd,
        showEntity.dayOfWeek?.toString(),
        showEntity.quarter?.toString(),
        showEntity.year)
}

enum class Day {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
}

enum class Quarter {
    WINTER, SPRING, SUMMER, FALL
}