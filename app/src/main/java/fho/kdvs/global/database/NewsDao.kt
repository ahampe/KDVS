package fho.kdvs.global.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.threeten.bp.LocalDate

@Dao
interface NewsDao {
    @Query("SELECT * from newsData")
    fun getAll(): LiveData<List<NewsEntity>>

    @Query("SELECT * from newsData where date >= :date")
    fun getAllNewsPastDate(date: LocalDate): LiveData<List<NewsEntity>>

    @Insert
    fun insert(newsEntity: NewsEntity)

    @Query("DELETE from newsData where title == :title and date == :date")
    fun deleteByTitleAndDate(title: String?, date: LocalDate?)

    @Query("DELETE from newsData")
    fun deleteAll()
}