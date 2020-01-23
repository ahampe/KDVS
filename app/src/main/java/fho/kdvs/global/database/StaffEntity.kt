package fho.kdvs.global.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Contacts page may change on a quarterly basis.
 */
@Entity(tableName = "staffData")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val staffId: Int = 0,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "position") var position: String? = null,
    @ColumnInfo(name = "email") var email: String? = null,
    @ColumnInfo(name = "duties") var duties: String? = null,
    @ColumnInfo(name = "officeHours") var officeHours: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        staffId = parcel.readInt(),
        name = parcel.readString(),
        position = parcel.readString(),
        email = parcel.readString(),
        duties = parcel.readString(),
        officeHours = parcel.readString()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeValue(staffId)
        dest?.writeValue(name)
        dest?.writeValue(position)
        dest?.writeValue(email)
        dest?.writeValue(duties)
        dest?.writeValue(officeHours)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<StaffEntity> {
            override fun createFromParcel(parcel: Parcel) = StaffEntity(parcel)

            override fun newArray(size: Int) = arrayOfNulls<StaffEntity>(size)
        }
    }
}