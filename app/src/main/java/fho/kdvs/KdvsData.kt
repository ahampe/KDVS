package fho.kdvs

import android.app.Application
import fho.kdvs.database.KdvsDatabase

/**
 * A singleton that will hold the database and related objects.
 * TODO replace with Dagger
 */
class KdvsData internal constructor(application: Application) {

    companion object {
        @Volatile
        private var INSTANCE: KdvsData? = null

        fun getInstance(): KdvsData =
            INSTANCE ?: throw IllegalStateException("Initialize from the application before usage!")

        fun initialize(application: Application): KdvsData {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KdvsData(application).also { INSTANCE = it }
            }
        }
    }

    val db: KdvsDatabase = KdvsDatabase.initialize(application)
}