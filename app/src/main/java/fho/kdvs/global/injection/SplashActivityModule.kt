package fho.kdvs.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fho.kdvs.global.SplashActivity

@Module
@Suppress("unused")
abstract class SplashActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeSplashActivity(): SplashActivity
}