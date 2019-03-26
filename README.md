# KDVS
A pure Kotlin Android app made for [KDVS](https://kdvs.org/) in Davis, CA. Streams the station live from anywhere with internet access, plays past broadcasts, and supports offline access.

## TODO
* Accurately determine currently playing show (assuming an arbitrary number of shows can occupy a single time slot)
* Reimplement home screen and migrate playback controls to a BottomSheetDialogFragment that can be opened from any screen
* Make scroll state consistent on the schedule view (when swiping between days)
* Handle HTML in show / broadcast descriptions

## Nice-to-haves
* Downloads management for past broadcasts
* Integration with other services (e.g., Spotify, Facebook)
* Search functionality (on hold currently; would require unacceptable network usage as it stands)
* Audio fingerprinting to determine the currently playing track (might be infeasible)

## Architecture
The app follows a [MVVM](https://developer.android.com/topic/libraries/architecture/viewmodel) architecture as encouraged by Google and supported with Android Jetpack. A single Activity is used in conjunction with the [Navigation Architecture Component](https://developer.android.com/topic/libraries/architecture/navigation) to simplify fragment management and streamline the passing of data between fragments. The [Room](https://developer.android.com/topic/libraries/architecture/room) ORM library is used for persisting scraped data. The underlying database is treated as the single source of truth throughout the app. Queries generally return either [RxJava](https://github.com/ReactiveX/RxJava) data types (when complicated transformations are needed) or [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) in simpler cases. Dependency injection is achieved with [Dagger2](https://google.github.io/dagger/android.html).
