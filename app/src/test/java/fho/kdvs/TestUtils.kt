package fho.kdvs

import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

object TestUtils {
    /** Loads a file from the test/resources directory. */
    fun loadFromResource(resName: String) = File(ClassLoader.getSystemResource(resName).path)

    /** Equality comparison between two objects of the same class, ignoring given properties. */
    inline fun <reified T : Any> T.isEqualIgnoringProperties(
        other: T,
        propertiesToIgnore: List<KProperty1<T, *>>
    ): Boolean {
        T::class.declaredMemberProperties.forEach { p ->
            if (!propertiesToIgnore.contains(p) && p.get(this) != p.get(other))
                return false
        }

        return true
    }
}