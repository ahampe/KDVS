package fho.kdvs.injection

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import fho.kdvs.global.KdvsApp
import fho.kdvs.global.SplashActivity
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        MainActivityModule::class,
        SplashActivityModule::class,
        ServiceModule::class
    ]
)
interface AppComponent : AndroidInjector<KdvsApp> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<KdvsApp>()
}
