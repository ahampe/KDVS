package fho.kdvs.global.preferences

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.enums.enumValueOrDefault
import fho.kdvs.schedule.QuarterYear
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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
open class KdvsPreferences @Inject constructor(application: Application) {

    companion object {
        const val FILE_NAME = "fho.kdvs.model.kdvspreferences"
    }

    // values are coupled to order in themes xml
    enum class Theme(val value: Int) {
        VIBRANT_DARK(0),
        VIBRANT_LIGHT(1),
        MUTED_DARK(2),
        MUTED_LIGHT(3)
    }

    enum class Key {
        // choice of mp3, ogg, aac
        STREAM_URL,

        // governs display of home fundraiser section based on 1, 2 or 3 month window
        FUNDRAISER_WINDOW,

        // last date/time of schedule scrape
        LAST_SCHEDULE_SCRAPE,

        // last date/time of show scrape (concat with show ID)
        LAST_SHOW_SCRAPE,

        // last date/time of broadcast scrape (concat with broadcast ID)
        LAST_BROADCAST_SCRAPE,

        // last date/time of news scrape (concat with page num)
        LAST_NEWS_SCRAPE,

        // last date/time of staff scrape
        LAST_STAFF_SCRAPE,

        // last date/time of top five adds scrape
        LAST_TOP_FIVE_ADDS_SCRAPE,

        // last date/time of top thirty albums scrape
        LAST_TOP_THIRTY_ALBUMS_SCRAPE,

        // last date/time of fundraiser scrape
        LAST_FUNDRAISER_SCRAPE,

        // scrape frequency (5, 15, 30, 60 minutes in seconds)
        SCRAPE_FREQUENCY,

        // to persist user's selection of quarter (internally a String) and year (an Int)
        SELECTED_QUARTER,
        SELECTED_YEAR,

        // TODO others like alert frequencies, wifi only usage, last played broadcast etc

        // amount of time between a notification and its related event
        NOTIFICATION_TIME,

        // data preferences
        ALLOWED_OVER_METERED,
        ALLOWED_OVER_ROAMING,

        // theme
        THEME,

        // download preferences
        DOWNLOAD_PATH
    }

    val preferences: SharedPreferences = application.getSharedPreferences(FILE_NAME, MODE_PRIVATE)

    var streamUrl: String? by StringPreference(Key.STREAM_URL)

    var fundraiserWindow: Int? by IntPreference(Key.FUNDRAISER_WINDOW)

    var lastScheduleScrape: Long? by LongPreference(Key.LAST_SCHEDULE_SCRAPE)

    var lastNewsScrape: Long? by LongPreference(Key.LAST_NEWS_SCRAPE)

    var lastStaffScrape: Long? by LongPreference(Key.LAST_STAFF_SCRAPE)

    var lastTopFiveAddsScrape: Long? by LongPreference(Key.LAST_TOP_FIVE_ADDS_SCRAPE)

    var lastTopThirtyAlbumsScrape: Long? by LongPreference(Key.LAST_TOP_THIRTY_ALBUMS_SCRAPE)

    var lastFundraiserScraper: Long? by LongPreference(Key.LAST_FUNDRAISER_SCRAPE)

    var allowedOverMetered: Boolean? by BooleanPreference(Key.ALLOWED_OVER_METERED)

    var allowedOverRoaming: Boolean? by BooleanPreference(Key.ALLOWED_OVER_ROAMING)

    var downloadPath: String? by StringPreference(Key.DOWNLOAD_PATH)

    var theme: Int? by IntPreference(Key.THEME)

    var notificationTime: Int? by IntPreference(Key.NOTIFICATION_TIME)

    fun getLastShowScrape(showId: String): Long? {
        val pref by LongPreference(Key.LAST_SHOW_SCRAPE, showId)
        return pref
    }

    fun setLastShowScrape(showId: String, value: Long) {
        var pref by LongPreference(Key.LAST_SHOW_SCRAPE, showId)
        // lint complains about this, but it is not aware that the setter delegate has an important side effect
        pref = value
    }

    fun getLastBroadcastScrape(broadcastId: String): Long? {
        val pref by LongPreference(Key.LAST_BROADCAST_SCRAPE, broadcastId)
        return pref
    }

    fun setLastBroadcastScrape(broadcastId: String, value : Long) {
        var pref by LongPreference(Key.LAST_BROADCAST_SCRAPE, broadcastId)
        // lint complains about this, but it is not aware that the setter delegate has an important side effect
        pref = value
    }

    /** Frequency of any type of scrape, in minutes. */
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
    inner class StringPreference(key: Key, suffix: String = "") : QuickPreference<String>(key, suffix) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            preferences.getString(key.name + suffix, null)
    }

    inner class BooleanPreference(key: Key, suffix: String = "") : QuickPreference<Boolean>(key, suffix) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean? =
            preferences.optBoolean(key.name + suffix)
    }

    inner class IntPreference(key: Key, suffix: String = "") : QuickPreference<Int>(key, suffix) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? =
            preferences.optInt(key.name + suffix)
    }

    inner class LongPreference(key: Key, suffix: String = "") : QuickPreference<Long>(key, suffix) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Long? =
            preferences.optLong(key.name + suffix)
    }

    abstract inner class QuickPreference<T>(
        internal val key: Key,
        internal val suffix: String
    ) : ReadWriteProperty<Any?, T?> {

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            val fullKey = key.name + suffix

            if (value == null) {
                preferences.edit().remove(fullKey).apply()
            } else {
                preferences.edit().apply {
                    when (value) {
                        is String -> putString(fullKey, value)
                        is Boolean -> putBoolean(fullKey, value)
                        is Long -> putLong(fullKey, value)
                        is Int -> putInt(fullKey, value)
                        is Float -> putFloat(fullKey, value)
                        else -> return
                    }
                }.apply()
            }
        }
    }

    inner class LiveSharedPreferences constructor(private val preferences: SharedPreferences) {

        private val publisher = PublishSubject.create<String>()
        private val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key -> publisher.onNext(key) }

        private val updates = publisher.doOnSubscribe {
            preferences.registerOnSharedPreferenceChangeListener(listener)
        }.doOnDispose {
            if (!publisher.hasObservers())
                preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }

        fun getPreferences(): SharedPreferences {
            return preferences
        }

        fun getString(key: String, defaultValue: String): LivePreference<String> {
            return LivePreference(updates, preferences, key, defaultValue)
        }

        fun getInt(key: String, defaultValue: Int): LivePreference<Int> {
            return LivePreference(updates, preferences, key, defaultValue)
        }

        fun getBoolean(key: String, defaultValue: Boolean): LivePreference<Boolean> {
            return LivePreference(updates, preferences, key, defaultValue)
        }

        fun getFloat(key: String, defaultValue: Float): LivePreference<Float> {
            return LivePreference(updates, preferences, key, defaultValue)
        }

        fun getLong(key: String, defaultValue: Long): LivePreference<Long> {
            return LivePreference(updates, preferences, key, defaultValue)
        }
    }

    inner class LivePreference<T> constructor(
        private val updates: Observable<String>,
        private val preferences: SharedPreferences,
        private val key: String,
        private val defaultValue: T
    ) : MutableLiveData<T>() {

        private var disposable: Disposable? = null

        override fun onActive() {
            super.onActive()
            value = preferences.all[key] as T ?: defaultValue

            disposable = updates.filter { t -> t == key }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object: DisposableObserver<String>() {
                    override fun onComplete() {

                    }

                    override fun onNext(t: String) {
                        postValue((preferences.all[t] as T) ?: defaultValue)
                    }

                    override fun onError(e: Throwable) {

                    }
                })
        }

        override fun onInactive() {
            super.onInactive()
            disposable?.dispose()
        }
    }
    // endregion
}
