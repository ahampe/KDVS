package fho.kdvs.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import fho.kdvs.extensions.toLiveData
import fho.kdvs.model.Day
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.repository.ShowRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    val showRepository: ShowRepository,
    application: Application
) : AndroidViewModel(application) {

    /** Signals the [ShowRepository] to scrape the schedule grid */
    fun fetchShows() = showRepository.fetchShows()

    fun getShowsForDay(day: Day): LiveData<List<ShowEntity>> =
        showRepository.getShowsForDay(day)
            .sample(500L, TimeUnit.MILLISECONDS, true)
            .toLiveData()

}