package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.threeten.bp.LocalDate

@Entity(tableName = "contactData")
data class NewsEntity (
    @ColumnInfo(name = "title") var title: String? = null,
    @ColumnInfo(name = "author") var author: String? = null,
    @ColumnInfo(name = "body") var body: String? = null,
    @ColumnInfo(name = "date") var date: LocalDate? = null,
    @ColumnInfo(name = "articleHref") var articleHref: String? = null,
    @ColumnInfo(name = "imageHrefs") var imageHrefs: List<String>? = null
)