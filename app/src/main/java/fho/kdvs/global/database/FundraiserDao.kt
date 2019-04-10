package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FundraiserDao {
    @Query("SELECT * from fundraiserData limit 1")
    fun get(): LiveData<FundraiserEntity>

    @Insert
    fun insert(fundraiserEntity: FundraiserEntity)

    @Query("DELETE from fundraiserData")
    fun deleteAll()
}