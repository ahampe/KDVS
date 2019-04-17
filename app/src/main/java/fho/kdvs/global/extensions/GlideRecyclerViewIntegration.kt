package fho.kdvs.global.extensions

import android.text.TextUtils
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.RequestBuilder


class MyPreloadModelProvider(val urls: List<String>, val fragment: Fragment) : PreloadModelProvider<Any> {

    override fun getPreloadItems(position: Int): List<String> {
        val url = urls.getOrNull(position) ?: ""
        return if (TextUtils.isEmpty(url)) listOf() else listOf(url)
    }

    override fun getPreloadRequestBuilder(item: Any): RequestBuilder<*> {
        return Glide.with(fragment)
            .load(item)
    }
}