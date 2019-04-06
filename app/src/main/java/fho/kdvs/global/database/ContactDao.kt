package fho.kdvs.global.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface ContactDao {
    @Query("SELECT * from contactData")
    fun getAll(): Flowable<List<ContactEntity>>

    @Insert
    fun insert(contactEntity: ContactEntity)

    @Query("DELETE from contactData")
    fun deleteAll()
}