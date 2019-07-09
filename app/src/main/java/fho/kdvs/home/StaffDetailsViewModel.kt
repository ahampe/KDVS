package fho.kdvs.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fho.kdvs.services.LiveShowUpdater
import javax.inject.Inject

class ScheduleSelectionViewModel @Inject constructor(
    private val liveShowUpdater: LiveShowUpdater,
    application: Application
) : AndroidViewModel(application) {

}