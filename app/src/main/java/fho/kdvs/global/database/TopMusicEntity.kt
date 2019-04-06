package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "topMusicData")
data class TopMusicEntity(
    @PrimaryKey(autoGenerate = true) var musicResourceId: Int = 0,
    @ColumnInfo(name = "weekOf") var weekOf: LocalDate? = null,
    @ColumnInfo(name = "position") var position: Int? = null,
    @ColumnInfo(name = "artist") var artist: String? = null,
    @ColumnInfo(name = "album") var album: String? = null,
    @ColumnInfo(name = "label") var label: String? = null,
    @ColumnInfo(name = "isNewAdd") var isNewAdd: Boolean? = null
)