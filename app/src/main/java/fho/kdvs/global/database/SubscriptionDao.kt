package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface SubscriptionDao {
    @Query("SELECT * from subscriptionData where showId = :showId LIMIT 1")
    fun getByShowId(showId: Int?): LiveData<SubscriptionEntity>

    @Query("SELECT * from subscriptionData")
    fun getAll(): List<SubscriptionEntity>

    @Insert(onConflict = REPLACE)
    fun insert(SubscriptionEntity: SubscriptionEntity)

    @Query("DELETE from subscriptionData where showId = :showId")
    fun deleteByShowId(showId: Int?)

    @Query("DELETE from subscriptionData")
    fun deleteAll()
}