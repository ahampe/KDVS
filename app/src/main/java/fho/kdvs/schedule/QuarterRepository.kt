package fho.kdvs.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.preferences.KdvsPreferences
import javax.inject.Inject
import javax.inject.Singleton

/** Repository for managing all [QuarterYear]s since the app's initialization. */
@Singleton
class QuarterRepository @Inject constructor(showDao: ShowDao, private val preferences: KdvsPreferences) {
    /** [LiveData] that observes all [QuarterYear]s, with the most recent first. */
    val allQuarterYearsLiveData: LiveData<List<QuarterYear>> by lazy { allQuarterYears.toLiveData() }

    /** [LiveData] that observes the selected [QuarterYear]. */
    val selectedQuarterYearLiveData: LiveData<QuarterYear> get() = _selectedQuarterYearLiveData

    // Fetches distinct quarter-years from the shows table, most recent first
    private val allQuarterYears =
        showDao.allDistinctQuarterYears()
            .filter { !it.isEmpty() }
            .distinctUntilChanged()

    /** [LiveData] that observes the currently selected [QuarterYear] */
    private val _selectedQuarterYearLiveData = MutableLiveData<QuarterYear>().apply {
        preferences.selectedQuarterYear?.let { postValue(it) }
    }

    /** Called whenever the user selects a new [QuarterYear] */
    fun selectQuarterYear(quarterYear: QuarterYear) {
        preferences.selectedQuarter = quarterYear.quarter
        preferences.selectedYear = quarterYear.year

        // Only fire distinct values to prevent redrawing UI
        if (quarterYear != _selectedQuarterYearLiveData.value) {
            _selectedQuarterYearLiveData.postValue(quarterYear)
        }
    }
}