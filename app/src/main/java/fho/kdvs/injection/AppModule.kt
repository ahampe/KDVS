package fho.kdvs.injection

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fho.kdvs.KdvsApp
import fho.kdvs.model.database.KdvsDatabase
import fho.kdvs.model.database.daos.ShowDao
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    fun provideApplication(app: KdvsApp): Application = app

    // TODO provide db, daos, scraper manager
    @Singleton
    @Provides
    fun provideDb(app: Application): KdvsDatabase =
        Room.databaseBuilder(app, KdvsDatabase::class.java, "kdvs.db")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideShowDao(db: KdvsDatabase): ShowDao = db.showDao()
}