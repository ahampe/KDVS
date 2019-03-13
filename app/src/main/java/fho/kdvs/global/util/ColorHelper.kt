package fho.kdvs.global.util
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import fho.kdvs.R
import java.util.*

object ColorHelper {
    private fun isGrayscalePixel(pixel: Int): Boolean {
        val alpha = (pixel and -0x1000000) shr 24
        val red = (pixel and 0x00FF0000) shr 16
        val green = (pixel and 0x0000FF00) shr 8
        val blue = pixel and 0x000000FF

        return (alpha == 0 && red == green && green == blue)
    }

    fun isGrayscaleImage(bitmap: Bitmap): Boolean {
        var isGrayscale = true
        for (i in 0..(bitmap.width-1)){
            for (j in 0..(bitmap.height-1)){
                if (!isGrayscalePixel(bitmap.getPixel(i,j))){
                    isGrayscale = false
                    break
                }
            }
        }
        return isGrayscale
    }

    fun getRandomMatColor(typeColor: Int, context: Context, seed: Long?): Int {
        var returnColor = ResourcesCompat.getColor(context.resources, R.color.colorAccent, context.theme)
        val arrayId = context.resources.getIdentifier(
            "mdcolor_" + typeColor.toString(),
            "array",
            context.packageName
        )

        if (arrayId != 0){
            val colors = context.resources.obtainTypedArray(arrayId)
            val random = Random(seed ?: Math.random().toLong())
            val index = (random.nextDouble() * colors.length()).toInt()
            returnColor = colors.getColor(index, returnColor)
            colors.recycle()
        }

        return returnColor
    }

/*    fun getColorFromId(id: Int, context: Context): Color {
        var default = ResourcesCompat.getColor(context.resources, R.color.colorAccent, context.theme)
        val colors = context.resources.obtainTypedArray(arrayId)
        val index = (random.nextDouble() * colors.length()).toInt()
        return colors.getColor(index, default)
    }*/
}
