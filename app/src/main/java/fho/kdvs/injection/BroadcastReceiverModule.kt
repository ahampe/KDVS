package fho.kdvs.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fho.kdvs.services.AlarmReceiver

@Suppress("unused")
@Module
abstract class BroadcastReceiverModule {
    @ContributesAndroidInjector
    abstract fun contributeAlarmReceiver() : AlarmReceiver
}