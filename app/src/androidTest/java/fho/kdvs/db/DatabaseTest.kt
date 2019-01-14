package fho.kdvs.db

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import fho.kdvs.database.KdvsDatabase
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
            InstrumentationRegistry.getInstrumentation().context,
            KdvsDatabase::class.java
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        countingTaskExecutorRule.drainTasks(10, TimeUnit.SECONDS)
        _db.close()
    }
}