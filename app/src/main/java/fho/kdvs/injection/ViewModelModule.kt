package fho.kdvs.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fho.kdvs.broadcast.BroadcastDetailsViewModel
import fho.kdvs.favorite.FavoriteViewModel
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.home.HomeViewModel
import fho.kdvs.player.PlayerViewModel
import fho.kdvs.schedule.ScheduleSelectionViewModel
import fho.kdvs.schedule.ScheduleViewModel
import fho.kdvs.schedule.ShowSearchViewModel
import fho.kdvs.show.ShowDetailsViewModel
import fho.kdvs.topmusic.TopMusicDetailsViewModel
import fho.kdvs.track.BroadcastTrackDetailsViewModel
import fho.kdvs.track.FavoriteTrackDetailsViewModel

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
    @ViewModelKey(PlayerViewModel::class)
    abstract fun bindPlayerViewModel(playerViewModel: PlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleViewModel::class)
    abstract fun bindScheduleViewModel(scheduleViewModel: ScheduleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleSelectionViewModel::class)
    abstract fun bindScheduleSelectionViewModel(scheduleSelectionViewModel: ScheduleSelectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShowSearchViewModel::class)
    abstract fun bindShowSearchViewModel(showSearchViewModel: ShowSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShowDetailsViewModel::class)
    abstract fun bindDetailsViewModel(showDetailsViewModel: ShowDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BroadcastDetailsViewModel::class)
    abstract fun bindBroadcastDetailsViewModel(broadcastDetailsViewModel: BroadcastDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BroadcastTrackDetailsViewModel::class)
    abstract fun bindBroadcastTrackDetailsViewModel(broadcastTrackDetailsViewModel: BroadcastTrackDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavoriteTrackDetailsViewModel::class)
    abstract fun bindFavoriteTrackDetailsViewModel(favoriteTrackDetailsViewModel: FavoriteTrackDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavoriteViewModel::class)
    abstract fun bindFavoriteViewModel(favoriteViewModel: FavoriteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TopMusicDetailsViewModel::class)
    abstract fun bindTopMusicDetailsViewModel(topMusicDetailsViewModel: TopMusicDetailsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: KdvsViewModelFactory): ViewModelProvider.Factory
}