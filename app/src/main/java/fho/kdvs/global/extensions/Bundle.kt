package fho.kdvs.global.extensions

import android.os.Bundle

fun Bundle.optInt(key: String): Int? =
    if (containsKey(key)) getInt(key) else null
