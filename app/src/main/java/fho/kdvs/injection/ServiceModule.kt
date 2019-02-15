package fho.kdvs.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fho.kdvs.services.AudioPlayerService

@Suppress("unused")
@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun provideAudioPlayerService(): AudioPlayerService
}