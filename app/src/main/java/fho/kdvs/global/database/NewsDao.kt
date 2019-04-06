package fho.kdvs.global.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable
import org.threeten.bp.LocalDate

@Dao
interface NewsDao {
    @Query("SELECT * from newsData")
    fun getAll(): Flowable<List<NewsEntity>>

    @Query("SELECT * from newsData where date >= :date")
    fun getAllNewsPastDate(date: LocalDate): Flowable<List<NewsEntity>>

    @Insert
    fun insert(newsEntity: NewsEntity)

    @Query("DELETE from newsData")
    fun deleteAll()
}