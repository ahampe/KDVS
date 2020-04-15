package fho.kdvs.injection

import android.app.Application
import android.content.ComponentName
import dagger.Module
import dagger.Provides
import fho.kdvs.services.AudioPlayerService
import fho.kdvs.services.MediaSessionConnection
import javax.inject.Singleton

@Module
class MediaModule {
    @Provides
    @Singleton
    fun provideMediaSessionConnection(app: Application) =
        MediaSessionConnection(app, ComponentName(app, AudioPlayerService::class.java))
}