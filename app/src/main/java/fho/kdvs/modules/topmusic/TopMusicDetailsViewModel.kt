package fho.kdvs.modules.topmusic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.global.database.TopMusicEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.threeten.bp.LocalDate
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TopMusicDetailsViewModel @Inject constructor(
    private val topMusicRepository: TopMusicRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    lateinit var liveTopMusic: LiveData<List<TopMusicEntity>>

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun initialize(weekOf: LocalDate?, type: TopMusicType) {
        if (weekOf != null)
            liveTopMusic = topMusicRepository.getTopAddsForWeekOf(weekOf, type)
        else if (type == TopMusicType.ALBUM)
            liveTopMusic = topMusicRepository.getMostRecentTopAlbums()
        else if (type == TopMusicType.ADD)
            liveTopMusic = topMusicRepository.getMostRecentTopAdds()
    }
}
