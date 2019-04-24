package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.show.ShowRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * [AndroidViewModel] for holding schedule-related data.
 * Scope this to a [ScheduleFragment][fho.kdvs.ui.fragments.ScheduleFragment] instance.
 */
class ScheduleViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val quarterRepository: QuarterRepository,
    application: Application
) : AndroidViewModel(application) {

    /** Signals the [ShowRepository] to scrape the schedule grid. */
    fun fetchShows() = showRepository.scrapeSchedule()

    fun getShowsForDay(day: Day, quarter: Quarter, year: Int): LiveData<List<TimeSlot>> =
        showRepository.getShowTimeSlotsForDay(day, quarter, year)
            .debounce (100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    /** All quarter-years in the database. */
    val allQuarterYearsLiveData = quarterRepository.allQuarterYearsLiveData

    /** The currently selected quarter-year. */
    val selectedQuarterYearLiveData = quarterRepository.selectedQuarterYearLiveData

    /** Sets the the given [QuarterYear]. Change will be reflected in [selectedQuarterYearLiveData]. */
    fun selectQuarterYear(quarterYear: QuarterYear) =
        quarterRepository.selectQuarterYear(quarterYear)

    /**
     * Gets the selected [QuarterYear] if there is one, else returns the most recent [QuarterYear].
     * If both are null, returns null.
     *
     * Note: It's important that [allQuarterYearsLiveData] has observers, otherwise it will not be updated.
     * This doesn't hold for [selectedQuarterYearLiveData] as it's not sourced from RxJava.
     */
    fun loadQuarterYear(): QuarterYear? =
        selectedQuarterYearLiveData.value ?: allQuarterYearsLiveData.value?.firstOrNull()

    /**
     * Called when a [TimeSlot] is clicked.
     * If the TimeSlot consists of a single show, navigates to [ShowDetailsFragment][fho.kdvs.show.ShowDetailsFragment].
     * Otherwise, navigates to a disambiguation fragment TODO
     * */
    fun onClickTimeSlot(navController: NavController, timeSlot: TimeSlot, index: Int) {
        val navAction = ScheduleFragmentDirections
            .actionScheduleFragmentToShowDetailsFragment(timeSlot.ids.getOrNull(index) ?: timeSlot.ids.first())
        navController.navigate(navAction)
    }
}