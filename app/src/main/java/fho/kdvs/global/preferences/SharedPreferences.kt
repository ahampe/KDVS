package fho.kdvs.global.preferences

import android.content.SharedPreferences

fun SharedPreferences.optInt(key: String): Int? = when {
    contains(key) -> getInt(key, 0) // defValue will not be reached
    else -> null
}

fun SharedPreferences.optLong(key: String): Long? = when {
    contains(key) -> getLong(key, 0L) // defValue will not be reached
    else -> null
}

fun SharedPreferences.optBoolean(key: String): Boolean? = when {
    contains(key) -> getBoolean(key, false) // defValue will not be reached
    else -> null
}