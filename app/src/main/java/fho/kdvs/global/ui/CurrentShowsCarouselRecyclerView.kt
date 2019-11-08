package fho.kdvs.global.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CurrentShowsCarouselRecyclerView(
    context: Context,
    attrs: AttributeSet
) : HorizontalCarouselRecyclerView(context, attrs) {
    private var button: Button? = null
    private val tag = "center"

    override fun <T : ViewHolder> initialize(newAdapter: Adapter<T>) {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        val recyclerView = this

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

                    // Scroll to center and fade-in to present current show seamlessly
                    adapter?.itemCount?.let {
                        val parent = (recyclerView.parent as LinearLayout)

                        smoothScrollToPosition(0)

                        parent.alpha = 0f
                        parent.visibility = View.VISIBLE
                        parent.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .setListener(null)
                    }

                    clearTags()
                    getChildAt(position).tag = tag

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

    override fun onScrollChanged() {
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

                // fade button in/out in sync with default view
                if (getChildAt(position).tag == tag) {
                    button?.let {
                        colorView(it, scaleValue)
                    }
                }
            }
        }
    }

    fun setButton(view: Button) {
        button = view
    }

    private fun clearTags() {
        for (i in 0..childCount) {
            getChildAt(i)?.let {
                it.tag = null
            }
        }
    }
}