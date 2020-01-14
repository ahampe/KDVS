package fho.kdvs.global.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class TimeslotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(timeslotEntity: TimeslotEntity)

    @Query("DELETE from timeslotData")
    abstract fun deleteAll()
}