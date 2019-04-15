package fho.kdvs.global.util
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import fho.kdvs.R
import java.util.*

object ColorHelper {
    fun getRandomMatColor(typeColor: Int, context: Context, seed: Long?): Int {
        var returnColor = ResourcesCompat.getColor(context.resources, R.color.colorAccent, context.theme)
        val arrayId = context.resources.getIdentifier(
            "mdcolor_" + typeColor.toString(),
            "array",
            context.packageName
        )

        if (arrayId != 0){
            val colors = context.resources.obtainTypedArray(arrayId)
            val random = Random(seed ?: (Math.random() * 100).toLong())
            val index = (random.nextDouble() * colors.length()).toInt()
            returnColor = colors.getColor(index, returnColor)
            colors.recycle()
        }

        return returnColor
    }
}
