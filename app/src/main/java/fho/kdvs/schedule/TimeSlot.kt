package fho.kdvs.schedule

import android.os.Parcel
import android.os.Parcelable
import fho.kdvs.global.database.ShowEntity
import org.threeten.bp.OffsetDateTime

/** Shows on the KDVS programming grid may share a single span of time each week and alternate.
 * This construct is built for holding information about individual TimeSlots, which may contain one or more shows. */
data class TimeSlot(
    val timeStart: OffsetDateTime?,
    val timeEnd: OffsetDateTime?,
    val isFirstHalfOrEntireSegment: Boolean,
    val imageHref: String?,
    val ids: List<Int>,
    val names: List<String?>
) : Parcelable {

    constructor(shows: List<ShowEntity>, _isFirstHalfOrEntireSegment: Boolean) : this(
        timeStart = shows.first().timeStart,
        timeEnd = shows.first().timeEnd,
        isFirstHalfOrEntireSegment = _isFirstHalfOrEntireSegment,
        imageHref = shows.first().defaultImageHref,
        ids = shows.map { it.id },
        names = shows.map { it.name }
    )

    constructor(parcel: Parcel) : this(
        timeStart = parcel.readValue(OffsetDateTime::class.java.classLoader) as OffsetDateTime?,
        timeEnd = parcel.readValue(OffsetDateTime::class.java.classLoader) as OffsetDateTime?,
        isFirstHalfOrEntireSegment = parcel.readValue(Boolean::class.java.classLoader) as Boolean,
        imageHref = parcel.readString(),
        ids = parcel.createIntArray()!!.toList(),
        names = parcel.createStringArray()!!.toList()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeValue(timeStart)
        dest?.writeValue(timeEnd)
        dest?.writeValue(isFirstHalfOrEntireSegment)
        dest?.writeString(imageHref)
        dest?.writeIntArray(ids.toIntArray())
        dest?.writeStringArray(names.toTypedArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<TimeSlot> {
            override fun createFromParcel(parcel: Parcel) = TimeSlot(parcel)

            override fun newArray(size: Int) = arrayOfNulls<TimeSlot>(size)
        }

        const val DUMMY_ID = -10
    }
}
