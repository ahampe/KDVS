package fho.kdvs.db

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fho.kdvs.model.database.KdvsDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.io.IOException
import java.util.concurrent.TimeUnit

abstract class DatabaseTest {
    @JvmField
    @Rule
    val countingTaskExecutorRule = CountingTaskExecutorRule()

    private lateinit var _db: KdvsDatabase
    val db: KdvsDatabase
        get() = _db

    @Before
    fun initDb() {
        _db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KdvsDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        countingTaskExecutorRule.drainTasks(10, TimeUnit.SECONDS)
        _db.close()
    }

    companion object {
        const val defaultTimeOut = 15L
    }
}