package fho.kdvs.repository

import fho.kdvs.database.daos.ShowDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepository @Inject constructor(private val showDao: ShowDao) {
    private val job = Job()

    private val dbScope = CoroutineScope(Dispatchers.IO + job)

    fun printShows() {
        dbScope.launch {
            val shows = showDao.getAll()
            Timber.d("shows got: $shows")
        }
    }
}