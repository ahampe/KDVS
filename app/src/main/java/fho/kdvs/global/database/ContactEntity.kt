package fho.kdvs.global.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactData")
data class ContactEntity (
    @PrimaryKey(autoGenerate = true) var contactId: Int = 0,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "position") var position: String? = null,
    @ColumnInfo(name = "email") var email: String? = null,
    @ColumnInfo(name = "duties") var duties: String? = null,
    @ColumnInfo(name = "officeHours") var officeHours: String? = null
)