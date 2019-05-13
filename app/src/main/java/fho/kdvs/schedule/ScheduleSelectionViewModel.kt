package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.services.LiveShowUpdater
import fho.kdvs.show.ShowRepository
import javax.inject.Inject

class ScheduleSelectionViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val liveShowUpdater: LiveShowUpdater,
    application: Application
) : AndroidViewModel(application) {

    lateinit var pairedIdsAndNames: List<Pair<Int, String>>
    lateinit var orderedShows: List<ShowEntity?>

    fun initialize(timeslot: TimeSlot) {
        //fetchShows()
        //orderedShows = getOrderedShowsForTimeslot(timeslot)
        pairedIdsAndNames = timeslot.ids.mapIndexed { index, i ->
            Pair(i, timeslot.names.getOrNull(index) ?: "")
        }
    }

    fun onClickShowSelection(navController: NavController, showId: Int) {
        val navAction = ScheduleFragmentDirections
            .actionScheduleFragmentToShowDetailsFragment(showId)

        navController.navigate(navAction)
    }

    private fun fetchShows(){
        showRepository.scrapeSchedule()
    }

    private fun getOrderedShowsForTimeslot(timeslot: TimeSlot): List<ShowEntity?> {
        return liveShowUpdater.orderShowsInTimeSlotRelativeToCurrentWeek(timeslot)
    }
}