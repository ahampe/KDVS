package fho.kdvs.global

import dagger.android.support.DaggerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class BaseFragment : DaggerFragment(), CoroutineScope {
    internal val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO
}