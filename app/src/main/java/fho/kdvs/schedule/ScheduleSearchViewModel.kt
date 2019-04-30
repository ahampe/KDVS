package fho.kdvs.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.global.database.ShowEntity
import javax.inject.Inject

class ScheduleSearchViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    fun onClickShow(navController: NavController, show: ShowEntity) {
        val navAction = ScheduleSearchFragmentDirections
            .actionScheduleSearchFragmentToShowDetailsFragment(show.id)
        navController.navigate(navAction)
    }
}