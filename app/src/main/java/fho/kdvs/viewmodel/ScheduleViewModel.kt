package fho.kdvs.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.extensions.toLiveData
import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.repository.ShowRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    application: Application
) : AndroidViewModel(application) {

    /** Signals the [ShowRepository] to scrape the schedule grid */
    fun fetchShows() = showRepository.fetchShows()

    fun getShowsForDay(day: Day, quarter: Quarter, year: Int): LiveData<List<ShowEntity>> =
        showRepository.getShowsForDay(day, quarter, year)
            .sample(500L, TimeUnit.MILLISECONDS, true)
            .toLiveData()
}