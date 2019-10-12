package fho.kdvs.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import fho.kdvs.global.database.*
import timber.log.Timber
import javax.inject.Singleton

@Suppress("unused")
@Module
class DatabaseModule(val app: Application) {

    val db = KdvsDatabase.buildDevelopmentDatabase(app)

    @Singleton
    @Provides
    fun provideDb(): KdvsDatabase {
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
    fun provideFavoriteDao(db: KdvsDatabase): FavoriteDao = db.favoriteDao()

    @Singleton
    @Provides
    fun provideSubscriptionDao(db: KdvsDatabase): SubscriptionDao = db.subscriptionDao()

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
    fun provideStaffDao(db: KdvsDatabase): StaffDao = db.staffDao()

    @Singleton
    @Provides
    fun provideFundraiserDao(db: KdvsDatabase): FundraiserDao = db.fundraiserDao()
}