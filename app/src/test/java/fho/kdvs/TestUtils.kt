package fho.kdvs

import java.io.File

object TestUtils {
    fun loadFromResource(resName: String) = File(ClassLoader.getSystemResource(resName).path)
}