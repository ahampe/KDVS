package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.show.ShowRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowSearchViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    application: Application
) : AndroidViewModel(application) {

    /** Signals the [ShowRepository] to scrape the schedule grid. */
    fun fetchShows() = showRepository.scrapeSchedule()

    fun getCurrentQuarterYear() : LiveData<QuarterYear> =
        showRepository.getCurrentQuarterYear()

    fun getShowsForCurrentQuarterYear(currentQuarterYear: QuarterYear) : LiveData<List<ShowEntity>> =
        showRepository.showsForQuarterYear(currentQuarterYear)
            .debounce ( 100L, TimeUnit.MILLISECONDS )
            .toLiveData()

    fun onClickShow(navController: NavController, show: ShowEntity) {
        val navAction = ShowSearchFragmentDirections
            .actionShowSearchFragmentToShowDetailsFragment(show.id)
        navController.navigate(navAction)
    }
}