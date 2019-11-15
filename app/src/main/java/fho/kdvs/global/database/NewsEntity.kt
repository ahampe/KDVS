package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "newsData", indices = [Index(value = arrayOf("body"), unique = true)])
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val newsId: Int = 0,
    @ColumnInfo(name = "title") var title: String? = null,
    @ColumnInfo(name = "author") var author: String? = null,
    @ColumnInfo(name = "body") var body: String? = null,
    @ColumnInfo(name = "date") var date: LocalDate? = null,
    @ColumnInfo(name = "articleHref") var articleHref: String? = null,
    @ColumnInfo(name = "imageHref") var imageHref: String? = null
)