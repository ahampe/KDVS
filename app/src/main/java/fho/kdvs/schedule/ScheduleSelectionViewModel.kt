package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.show.ShowRepository
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

class ScheduleSelectionViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    application: Application
) : AndroidViewModel(application) {

    fun initialize() {
        fetchShows()
    }

    fun onClickShowSelection(navController: NavController, showId: Int) {
        val navAction = ScheduleFragmentDirections
            .actionScheduleFragmentToShowDetailsFragment(showId)

        navController.navigate(navAction)
    }

    private fun fetchShows(){
        showRepository.scrapeSchedule()
    }

    suspend fun allOrderedShowsForTime(timeStart: OffsetDateTime): List<ShowEntity?>{
        return showRepository.allShowsAtTimeOrderedRelativeToCurrentWeek(timeStart)
    }

    fun getPairedIdsAndNamesForShows(shows: List<ShowEntity?>): List<Pair<Int, String>> {
        return shows
            .map { s -> s!!.id }
            .mapIndexed { index, i ->
                Pair(i, shows
                    .map{ s -> s!!.name }
                    .toList()
                    .getOrNull(index) ?: "")
            }
    }
}