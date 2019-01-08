package fho.kdvs.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fho.kdvs.database.models.Day
import fho.kdvs.database.models.Quarter
import java.util.*

@Entity(tableName = "showData")
data class ShowEntity(
    @PrimaryKey(autoGenerate = false) val id: Int,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "host") var host: String? = null,
    @ColumnInfo(name = "genre") var genre: String? = null,
    @ColumnInfo(name = "defaultDesc") var defaultDesc: String? = null,
    @ColumnInfo(name = "defaultImageHref") var defaultImageHref: String? = null,
    @ColumnInfo(name = "timeStart") var timeStart: Date? = null,
    @ColumnInfo(name = "timeEnd") var timeEnd: Date? = null,
    @ColumnInfo(name = "dayOfWeek") var dayOfWeek: Day? = null,
    @ColumnInfo(name = "quarter") var quarter: Quarter? = null,
    @ColumnInfo(name = "year") var year: Int? = null
) {
//    constructor() : this(0, "", "", "", "", null, null, null, null, 0)
//    constructor(show: Show) : this() {
//        id = show.id
//        name = show.name
//        host = show.host
//        genre = show.genre
//        defaultDesc = show.defaultDesc
//        timeStart = show.timeStart
//        timeEnd = show.timeEnd
//        dayOfWeek = show.dayOfWeek?.let { Day.valueOf(it.toUpperCase()) }
//        quarter = show.quarter?.let { Quarter.valueOf(it.toUpperCase()) }
//        year = show.year
//    }
}