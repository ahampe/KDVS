package fho.kdvs.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import fho.kdvs.extensions.isMainThread

/**
 * Represents a UI event.  This should be used as the value posted by a [UiSimpleEventLiveData] or [UiEventLiveData].
 *
 * [UiSimpleEventLiveData] and [UiEventLiveData] are useful for cases where you need to notify an activity or fragment
 * of an action from the ViewModel.  For example let's say in your view model you have a function which is triggered
 * in response to a button click which fetches data from the network.  You now need to notify the fragment to display
 * a new view in response to this.  The view model would expose a [UiSimpleEventLiveData] or [UiEventLiveData] which the
 * fragment will be listening to.  Setting a new event on the live data would then trigger the listener on the fragment
 * allowing the UI to be displayed within the fragments context.
 *
 * See [this article][https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150]
 */
open class UiEvent<out T>(val content: T) {

    /**
     * The live data which vends this event might have multiple listeners.  This boolean ensures that the event is
     * only handled once.
     */
    private var hasBeenHandled = false

    /** Returns the content and prevents its use again. */
    fun handle(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

/**
 * A live data used for emitting [UiEvent].
 *
 * This class is a subclass of [SingleLiveData], so it will only be notified of events which happen after it's being
 * being observed.  The given type [T] will be wrapped in a [UiEvent].
 */
open class UiEventLiveData<T> : SingleLiveData<UiEvent<T>>() {
    /** Emits a [UiEvent] with the given value.  This is safe to call on any thread. */
    fun trigger(content: T) {
        val event = UiEvent(content)
        if (isMainThread()) {
            setValue(event)
        } else {
            postValue(event)
        }
    }
}

/** See [UiEventLiveData].  Use this when you don't need to pass along data in the [UiEvent] */
class UiSimpleEventLiveData : UiEventLiveData<Unit>() {
    /** Emits a [UiEvent].  This is safe to call on any thread. */
    fun trigger() = trigger(Unit)
}

open class SingleLiveData<T> : LiveData<T>() {
    private var version = 0

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val observerVersion = version
        super.observe(owner, Observer { v ->
            if (observerVersion <= version) {
                observer.onChanged(v)
            }
        })
    }

    override fun setValue(value: T) {
        super.setValue(value)
        version++
    }
}