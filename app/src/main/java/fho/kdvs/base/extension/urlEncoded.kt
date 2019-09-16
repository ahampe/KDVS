package fho.kdvs.base.extension

import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * Helper extension to URL encode a [String]. Returns an empty string when called on null.
 */
inline val String?.urlEncoded: String
    get() = if (Charset.isSupported("UTF-8")) {
        URLEncoder.encode(this ?: "", "UTF-8")
    } else {
        // If UTF-8 is not supported, use the default charset.
        @Suppress("deprecation")
        (URLEncoder.encode(this ?: ""))
    }
