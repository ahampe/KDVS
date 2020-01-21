package fho.kdvs

import org.mockito.Mockito
import java.io.File

object TestUtils {
    /** Loads a file from the test/resources directory. */
    fun loadFromResource(resName: String) = File(ClassLoader.getSystemResource(resName).path)

    /** Convenience function that avoids Mockito's annoying NPE for Kotlin classes in tests */
    fun <T> any(): T = Mockito.any<T>()
}