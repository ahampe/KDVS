package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import fho.kdvs.global.database.joins.ShowTimeslotsJoin
import fho.kdvs.global.extensions.toLiveData
import fho.kdvs.show.ShowRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowSearchViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    application: Application
) : AndroidViewModel(application) {

    fun getShowTimeslotsJoinsForCurrentQuarterYear(currentQuarterYear: QuarterYear): LiveData<List<ShowTimeslotsJoin>> =
        showRepository.showTimeslotsJoinsByQuarterYear(currentQuarterYear)
            .debounce(100L, TimeUnit.MILLISECONDS)
            .toLiveData()

    fun onClickShow(navController: NavController, join: ShowTimeslotsJoin) {
        join.show?.id?.let {
            val navAction = ShowSearchFragmentDirections
                .actionShowSearchFragmentToShowDetailsFragment(it)
            navController.navigate(navAction)
        }
    }
}