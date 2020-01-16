package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.ShowTimeslotEntity
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.show.ShowRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowSearchViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    application: Application
) : AndroidViewModel(application) {

    fun getShowTimeslotsForCurrentQuarterYear(currentQuarterYear: QuarterYear): LiveData<List<ShowTimeslotEntity>> =
        showRepository.showsForQuarterYear(currentQuarterYear)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    fun onClickShow(navController: NavController, show: ShowTimeslotEntity) {
        val navAction = ShowSearchFragmentDirections
            .actionShowSearchFragmentToShowDetailsFragment(show.id)
        navController.navigate(navAction)
    }
}