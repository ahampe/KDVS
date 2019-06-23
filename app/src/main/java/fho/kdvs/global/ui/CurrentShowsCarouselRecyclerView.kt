package fho.kdvs.global.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cell_current_show.view.*

class CurrentShowsCarouselRecyclerView(
    context: Context,
    attrs: AttributeSet
): HorizontalCarouselRecyclerView(context, attrs) {
    private var button: Button? = null
    private var label: TextView? = null
    private var positionToTextMap: Map<Int, String>? = null
    private val tag = "center"

    override fun <T : ViewHolder> initialize(newAdapter: Adapter<T>) {
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
                    getChildAt(_defaultPos).tag = tag
                    addOnScrollListener(object : OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            onScrollChanged()
                        }

                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (newState == SCROLL_STATE_IDLE) {
                                val position = getCurrentItem()
                                onItemChanged(position)
                            }
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

                _viewsToChangeColor.forEach { viewId ->
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

    fun setLabel(view: TextView) {
        label = view
    }

    fun setPositionToTextMap(map: Map<Int, String>) {
        positionToTextMap = map
    }

    private fun getCurrentItem(): Int {
        return (this.layoutManager as LinearLayoutManager)
            .findFirstVisibleItemPosition()
    }

    private fun onItemChanged(position: Int) {
        label?.text = positionToTextMap?.get(position) ?: ""
    }
}