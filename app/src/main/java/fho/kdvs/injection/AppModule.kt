package fho.kdvs.injection

import android.app.Application
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import dagger.Module
import dagger.Provides
import fho.kdvs.global.KdvsApp
import fho.kdvs.global.database.*
import timber.log.Timber
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

    @Singleton
    @Provides
    fun provideDb(app: Application): KdvsDatabase {
        val db = KdvsDatabase.buildDevelopmentDatabase(app)

        // Delete and rebuild the database if there's a schema change so that we don't have to bump versions or uninstall
        try {
            db.openHelper.readableDatabase
        } catch (ise: IllegalStateException) {
            Timber.e("Detected a schema modification.  Deleting the database file...")
            db.close()
            KdvsDatabase.deleteDatabaseFile(app)
        }

        return if (db.isOpen) db else KdvsDatabase.buildDevelopmentDatabase(app)
    }


    @Singleton
    @Provides
    fun provideShowDao(db: KdvsDatabase): ShowDao = db.showDao()

    @Singleton
    @Provides
    fun provideBroadcastDao(db: KdvsDatabase): BroadcastDao = db.broadcastDao()

    @Singleton
    @Provides
    fun provideTrackDao(db: KdvsDatabase): TrackDao = db.trackDao()

    @Singleton
    @Provides
    fun provideNewsDao(db: KdvsDatabase): NewsDao = db.newsDao()

    @Singleton
    @Provides
    fun provideTopMusicDao(db: KdvsDatabase): TopMusicDao = db.topMusicDao()

    @Singleton
    @Provides
    fun provideContactDao(db: KdvsDatabase): StaffDao = db.contactDao()
}