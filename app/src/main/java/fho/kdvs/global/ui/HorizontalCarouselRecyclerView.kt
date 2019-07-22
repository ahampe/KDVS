package fho.kdvs.global.ui

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.R
import kotlin.math.E
import kotlin.math.pow

/** https://medium.com/@supahsoftware/custom-android-views-carousel-recyclerview-7b9318d23e9a */
open class HorizontalCarouselRecyclerView(
    context: Context,
    attrs: AttributeSet
) : RecyclerView(context, attrs) {
    private val accentColor
            by lazy { ContextCompat.getColor(context, R.color.colorAccent) }
    private val activeColor
            by lazy { ContextCompat.getColor(context, R.color.colorWhite) }
    private val inactiveColor
            by lazy { ContextCompat.getColor(context, R.color.colorTransparent) }
    protected var colorViews = listOf<Int>()
    protected var position = 0

    open fun <T : ViewHolder> initialize(newAdapter: Adapter<T>) {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        newAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                onChanged()
            }

            override fun onChanged() {
                post {
                    if (childCount == 0) return@post

                    val sidePadding = (width / 2) - (getChildAt(0).width / 2)
                    setPadding(sidePadding, 0, sidePadding, 0)
                    smoothScrollToPosition(position)
                    addOnScrollListener(object : OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            onScrollChanged()
                        }
                    })
                }
            }
        })

        adapter = newAdapter
    }

    fun setDefaultPos(pos: Int) {
        position = pos
    }

    fun setViewsToChangeColor(viewIds: List<Int>) {
        colorViews = viewIds
    }

    open fun onScrollChanged() {
        post {
            (0 until childCount).forEach { position ->
                val child = getChildAt(position)
                val childCenterX = (child.left + child.right) / 2
                val scaleValue = getGaussianScale(childCenterX, 1f, 1f, 150.toDouble())

                child.scaleX = scaleValue
                child.scaleY = scaleValue

                colorViews.forEach { viewId ->
                    val view = child.findViewById<View>(viewId)
                    colorView(view, scaleValue)
                }
            }
        }
    }

    protected fun colorView(view: View, scaleValue: Float) {
        val saturationPercent = (scaleValue - 1) / 1f
        val alphaPercent = scaleValue / 2f
        val matrix = ColorMatrix()

        matrix.setSaturation(saturationPercent)

        when (view) {
            is Button -> {
                val textColor = ArgbEvaluator().evaluate(saturationPercent, inactiveColor, activeColor) as Int
                val bgColor = ArgbEvaluator().evaluate(saturationPercent, inactiveColor, accentColor) as Int
                view.setTextColor(textColor)
                view.setBackgroundColor(bgColor)
            }
            is ImageView -> {
                view.colorFilter = ColorMatrixColorFilter(matrix)
                view.imageAlpha = (255 * alphaPercent).toInt()
            }
            is TextView -> {
                val textColor = ArgbEvaluator().evaluate(saturationPercent, inactiveColor, activeColor) as Int
                view.setTextColor(textColor)
            }
        }
    }

    protected fun getGaussianScale(
        childCenterX: Int,
        minScaleOffset: Float,
        scaleFactor: Float,
        spreadFactor: Double
    ): Float {
        val recyclerCenterX = (left + right) / 2

        return (E.pow(-(childCenterX - recyclerCenterX.toDouble()).pow(2) / (2 * spreadFactor.pow(2)))
                * scaleFactor + minScaleOffset)
            .toFloat()
    }
}