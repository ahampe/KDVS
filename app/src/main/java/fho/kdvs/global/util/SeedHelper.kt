package fho.kdvs.global.util

object SeedHelper {
    private fun stripExtension(str: String): String {
        val index = str.lastIndexOf('.')
        return if (index == -1) str else str.substring(0, index)
    }

    /**
     * Trim string past last '.', if any.
     * Read 8 bytes from end. Convert to numeric value.
     */
    fun getSeedFromStr(str: String): Long {
        if (str.isEmpty()) return Math.random().toLong()

        var value: Long = 0
        val trimmedStr = stripExtension(str)
        val bytes = trimmedStr.toByteArray()
        val sigBytes = bytes.filterIndexed { index, _ ->
            index >= bytes.size - 8 }

        for (i in 0..(sigBytes.size-1)) {
            value = (value shl 8) + (bytes[i].toLong() and 0xffL)
        }

        if (value == 0L) return Math.random().toLong()

        return value
    }
}