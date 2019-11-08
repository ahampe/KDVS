package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "fundraiserData")
data class FundraiserEntity(
    @PrimaryKey(autoGenerate = true) val fundraiserId: Int = 0,
    @ColumnInfo(name = "goal") var goal: Int? = null,
    @ColumnInfo(name = "current") var current: Int? = null,
    @ColumnInfo(name = "dateStart") var dateStart: LocalDate? = null,
    @ColumnInfo(name = "dateEnd") var dateEnd: LocalDate? = null
)