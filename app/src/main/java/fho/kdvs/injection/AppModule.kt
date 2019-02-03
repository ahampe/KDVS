package fho.kdvs.injection

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fho.kdvs.global.KdvsApp
import fho.kdvs.global.database.BroadcastDao
import fho.kdvs.global.database.KdvsDatabase
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.database.TrackDao
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    fun provideApplication(app: KdvsApp): Application = app

    // TODO don't fallback to destructive migration in production
    @Singleton
    @Provides
    fun provideDb(app: Application): KdvsDatabase =
        Room.databaseBuilder(app, KdvsDatabase::class.java, "kdvs.db")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideShowDao(db: KdvsDatabase): ShowDao = db.showDao()

    @Singleton
    @Provides
    fun provideBroadcastDao(db: KdvsDatabase): BroadcastDao = db.broadcastDao()

    @Singleton
    @Provides
    fun provideTrackDao(db: KdvsDatabase): TrackDao = db.trackDao()
}