package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fho.kdvs.global.enums.Quarter

/**
 * Note: A show here is defined as: a program specific to a given [Quarter] and [Year], unique on
 * the basis of its name, that may occur in multiple [TimeslotEntity]'s a week in the case of
 * syndicated programs (e.g. Democracy Now) or once every n weeks in a given [TimeslotEntity] in
 * the case of alternating programs.
 *
 * Time information for a [ShowEntity] is contained in its associated [TimeslotEntity]s.
 */
@Entity(tableName = "showData")
data class ShowEntity(
    @PrimaryKey(autoGenerate = false) override val id: Int,
    @ColumnInfo(name = "name") override var name: String? = null,
    @ColumnInfo(name = "host") override var host: String? = null,
    @ColumnInfo(name = "genre") override var genre: String? = null,
    @ColumnInfo(name = "defaultDesc") override var defaultDesc: String? = null,
    @ColumnInfo(name = "defaultImageHref") override var defaultImageHref: String? = null,
    @ColumnInfo(name = "quarter") override var quarter: Quarter? = null,
    @ColumnInfo(name = "year") override var year: Int? = null
): Show()