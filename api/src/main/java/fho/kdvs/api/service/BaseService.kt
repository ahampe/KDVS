package fho.kdvs.api.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Base class for providing services, which call Retrofit endpoints via suspending methods
 */
abstract class BaseService : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val job = Job()

}
