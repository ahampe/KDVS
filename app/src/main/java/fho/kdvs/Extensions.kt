package fho.kdvs

import android.content.Context
import android.net.ConnectivityManager

fun Context.isConnectedToInternet(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val activeNetwork = cm.activeNetworkInfo

    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}