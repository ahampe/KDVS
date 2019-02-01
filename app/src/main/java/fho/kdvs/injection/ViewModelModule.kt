package fho.kdvs.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fho.kdvs.viewmodel.KdvsViewModel
import fho.kdvs.viewmodel.KdvsViewModelFactory
import fho.kdvs.viewmodel.ScheduleViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(KdvsViewModel::class)
    abstract fun bindKdvsViewModel(kdvsViewModel: KdvsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleViewModel::class)
    abstract fun bindScheduleViewModel(scheduleViewModel: ScheduleViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: KdvsViewModelFactory): ViewModelProvider.Factory
}