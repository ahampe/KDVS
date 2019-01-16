package fho.kdvs

import org.mockito.Mockito
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object TestUtils {
    /** Loads a file from the test/resources directory. */
    fun loadFromResource(resName: String) = File(ClassLoader.getSystemResource(resName).path)

    /** Convenience function that avoids Mockito's annoying NPE for Kotlin classes in tests */
    fun <T> any(): T = Mockito.any<T>()

    /** Returns a Date object given a time in 24-hour (HH:mm) format. */
    fun makeDateFromTime(time: String): Date = SimpleDateFormat("HH:mm").parse(time)

    /** Returns a Date object given a date string in MM/dd/yyyy format */
    fun makeDate(date: String): Date = SimpleDateFormat("MM/dd/yyy").parse(date)
}