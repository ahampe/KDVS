package fho.kdvs.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fho.kdvs.broadcast.BroadcastDetailsViewModel
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.home.HomeViewModel
import fho.kdvs.schedule.ScheduleViewModel
import fho.kdvs.show.ShowDetailsViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SharedViewModel::class)
    abstract fun bindKdvsViewModel(sharedViewModel: SharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleViewModel::class)
    abstract fun bindScheduleViewModel(scheduleViewModel: ScheduleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShowDetailsViewModel::class)
    abstract fun bindDetailsViewModel(showDetailsViewModel: ShowDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BroadcastDetailsViewModel::class)
    abstract fun bindBroadcastDetailsViewModel(broadcastDetailsViewModel: BroadcastDetailsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: KdvsViewModelFactory): ViewModelProvider.Factory
}