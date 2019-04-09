package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fho.kdvs.schedule.QuarterYear

/**
 * Contacts page may change on a quarterly basis.
 */
@Entity(tableName = "contactData")
data class StaffEntity (
    @PrimaryKey(autoGenerate = true) var contactId: Int = 0,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "position") var position: String? = null,
    @ColumnInfo(name = "email") var email: String? = null,
    @ColumnInfo(name = "duties") var duties: String? = null,
    @ColumnInfo(name = "officeHours") var officeHours: String? = null
)