package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StaffDao {
    @Query("SELECT * from staffData")
    fun getAll(): LiveData<List<StaffEntity>>

    @Insert
    fun insert(staffEntity: StaffEntity)

    @Query("DELETE from staffData")
    fun deleteAll()
}