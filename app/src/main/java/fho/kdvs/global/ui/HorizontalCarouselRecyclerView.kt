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

/** https://medium.com/@supahsoftware/custom-android-views-carousel-recyclerview-7b9318d23e9a */
abstract class HorizontalCarouselRecyclerView(
    context: Context,
    attrs: AttributeSet
) : RecyclerView(context, attrs) {
    private val activeColor
            by lazy { ContextCompat.getColor(context, R.color.colorWhite) }
    private val inactiveColor
            by lazy { ContextCompat.getColor(context, R.color.colorTransparent) }
    protected var _viewsToChangeColor = listOf<Int>()
    protected var _defaultPos = 0

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
                    smoothScrollToPosition(_defaultPos)
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
        _defaultPos = pos
    }

    fun setViewsToChangeColor(viewIds: List<Int>) {
        _viewsToChangeColor = viewIds
    }

    open fun onScrollChanged() {
        post {
            (0 until childCount).forEach { position ->
                val child = getChildAt(position)
                val childCenterX = (child.left + child.right) / 2
                val scaleValue = getGaussianScale(childCenterX, 1f, 1f, 150.toDouble())

                child.scaleX = scaleValue
                child.scaleY = scaleValue

                _viewsToChangeColor.forEach { viewId ->
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
                view.background.colorFilter = ColorMatrixColorFilter(matrix)
                view.alpha = (255 * alphaPercent)

                val textColor = ArgbEvaluator().evaluate(saturationPercent, inactiveColor, activeColor) as Int
                view.setTextColor(textColor)
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
        minScaleOffest: Float,
        scaleFactor: Float,
        spreadFactor: Double
    ): Float {
        val recyclerCenterX = (left + right) / 2

        return (Math.pow(
            Math.E,
            -Math.pow(childCenterX - recyclerCenterX.toDouble(), 2.toDouble()) / (2 * Math.pow(
                spreadFactor,
                2.toDouble()
            ))
        ) * scaleFactor + minScaleOffest).toFloat()
    }
}