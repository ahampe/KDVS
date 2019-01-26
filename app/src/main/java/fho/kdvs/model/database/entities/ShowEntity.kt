package fho.kdvs.model.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
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
)