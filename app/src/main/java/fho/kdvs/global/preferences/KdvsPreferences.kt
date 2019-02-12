package fho.kdvs.global.preferences

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.enums.enumValueOrDefault
import fho.kdvs.schedule.QuarterYear
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * This class exposes a public API for getting and setting [SharedPreferences].
 *
 * It's assumed that setting a value to null means the preference should be cleared.
 */
@Singleton
class KdvsPreferences @Inject constructor(application: Application) {

    companion object {
        const val FILE_NAME = "fho.kdvs.model.kdvspreferences"
    }

    enum class Key {
        // choice of mp3, ogg, aac
        STREAM_URL,

        // last date/time of scrape
        LAST_SCRAPE,

        // scrape frequency (5, 15, 30, 60 minutes?)
        SCRAPE_FREQUENCY,

        // to persist user's selection of quarter (internally a String) and year (an Int)
        SELECTED_QUARTER,
        SELECTED_YEAR

        // TODO others like alert frequencies, wifi only usage, etc
    }

    private val preferences: SharedPreferences = application.getSharedPreferences(FILE_NAME, MODE_PRIVATE)

    var streamUrl: String? by StringPreference(Key.STREAM_URL)

    var lastScrapeDate: Long? by LongPreference(Key.LAST_SCRAPE)

    var scrapeFrequency: Long? by LongPreference(Key.SCRAPE_FREQUENCY)

    /** Convenience property to get the selected [QuarterYear] */
    var selectedQuarterYear: QuarterYear?
        get() {
            val quarter = selectedQuarter
            val year = selectedYear
            return if (quarter != null && year != null) {
                QuarterYear(quarter, year)
            } else null
        }
        set(value) {
            selectedQuarter = value?.quarter
            selectedYear = value?.year
        }

    var selectedQuarter: Quarter?
        get() = enumValueOrDefault(_selectedQuarter, Quarter.WINTER)
        set(value) {
            _selectedQuarter = value?.name
        }

    private var _selectedQuarter: String? by StringPreference(Key.SELECTED_QUARTER)

    var selectedYear: Int? by IntPreference(Key.SELECTED_YEAR)

    /** Clears everything from shared preferences. */
    fun clearAll() = preferences.edit().clear().apply()

    // region property delegates
    inner class StringPreference(key: Key) : QuickPreference<String>(key) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            preferences.getString(key.name, null)
    }

    inner class BooleanPreference(key: Key) : QuickPreference<Boolean>(key) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean? = preferences.optBoolean(key.name)
    }

    inner class IntPreference(key: Key) : QuickPreference<Int>(key) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? = preferences.optInt(key.name)
    }

    inner class LongPreference(key: Key) : QuickPreference<Long>(key) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Long? = preferences.optLong(key.name)
    }

    abstract inner class QuickPreference<T>(
        internal val key: Key
    ) : ReadWriteProperty<Any?, T?> {

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            if (value == null) {
                preferences.edit().remove(key.name).apply()
            } else {
                preferences.edit().apply {
                    when (value) {
                        is String -> putString(key.name, value)
                        is Boolean -> putBoolean(key.name, value)
                        is Long -> putLong(key.name, value)
                        is Int -> putInt(key.name, value)
                        is Float -> putFloat(key.name, value)
                        else -> return
                    }
                }.apply()
            }
        }
    }
    // endregion
}
