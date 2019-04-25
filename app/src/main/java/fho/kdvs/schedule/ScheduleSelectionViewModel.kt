package fho.kdvs.show

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.schedule.TimeSlot
import fho.kdvs.services.LiveShowUpdater
import javax.inject.Inject

class ScheduleSelectionViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val liveShowUpdater: LiveShowUpdater,
    application: Application
) : AndroidViewModel(application) {

    lateinit var orderedShows: List<ShowEntity?>

    suspend fun initialize(timeslot: TimeSlot) {
        fetchShows()
        orderedShows = getOrderedShowsForTimeslot(timeslot)
    }

//    fun onClickShow(navController: NavController) {
//        val navAction = ShowDetailsFragmentDirections
//            .actionShowDetailsFragmentToShowDetailsFragment()
//        navController.navigate(navAction)
//    }

    private fun fetchShows(){
        showRepository.scrapeSchedule()
    }

    private suspend fun getOrderedShowsForTimeslot(timeslot: TimeSlot): List<ShowEntity?> {
        return liveShowUpdater.orderShowsInTimeSlotRelativeToCurrentWeek(timeslot)
    }
}