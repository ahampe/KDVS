package fho.kdvs.global.extensions

import android.graphics.*
import android.graphics.Shader.TileMode
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


class ReflectionRequestListener (
    private val view: ImageView
) : RequestListener<Bitmap> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Bitmap>,
        isFirstResource: Boolean
    ) : Boolean {

        return false
    }

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any,
        target: Target<Bitmap>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (resource != null) {
            val imageWithReflection = applyReflection(resource)
            if (imageWithReflection != null)
                view.setImageBitmap(imageWithReflection)
        }
        return false
    }

    private fun applyReflection(image: Bitmap): Bitmap? {
        val reflectionGap = 0
        val reflectionHeight = image.height / 4

        // Reflect across x-axis
        val matrix = Matrix()
        matrix.preScale(1.toFloat(), (-1).toFloat())

        // Apply matrix, get bottom half
        val reflection = Bitmap.createBitmap(
            image,
            0,
            reflectionHeight,
            image.width,
            reflectionHeight,
            matrix,
            false
        )

        val imageWithReflection = Bitmap.createBitmap(
            image.width,
            image.height + reflectionHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(imageWithReflection)
        canvas.drawBitmap(reflection, 0f, 0f, null)

        val paint = Paint()
        val shader = LinearGradient(
            0f,
            image.height.toFloat(),
            0f,
            (imageWithReflection.height + reflectionGap).toFloat(),
            0x99ffffff.toInt(),
            0x00ffffff,
            TileMode.CLAMP
        )
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(
            0f,
            image.height.toFloat(),
            image.width.toFloat(),
            (imageWithReflection.height + reflectionGap).toFloat(),
            paint)

        return imageWithReflection
    }
}