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
import timber.log.Timber
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    fun provideApplication(app: KdvsApp): Application = app

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
}