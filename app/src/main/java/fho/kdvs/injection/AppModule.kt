package fho.kdvs.injection

import android.app.Application
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import dagger.Module
import dagger.Provides
import fho.kdvs.global.KdvsApp
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class, MediaModule::class])
class AppModule {
    @Provides
    fun provideApplication(app: KdvsApp): Application = app

    @Singleton
    @Provides
    fun provideExoPlayer(app: Application): ExoPlayer = ExoPlayerFactory.newSimpleInstance(app).apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(), true
        )
    }
}