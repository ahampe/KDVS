package fho.kdvs

import java.io.File

object TestUtils {
    /** Loads a file from the test/resources directory. */
    fun loadFromResource(resName: String) = File(ClassLoader.getSystemResource(resName).path)
}