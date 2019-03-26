package fho.kdvs.global.behavior

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import fho.kdvs.R
import kotlin.math.max
import kotlin.math.min

@Suppress("unused") // used in XML
class BottomNavigationBehavior<V : View>(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<V>(context, attrs) {

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        val translation = max(0f, min(child.height.toFloat(), child.translationY + dy))
        child.translationY = translation

        // Also bring the bottom sheet down with it
        val nowPlayingView = coordinatorLayout.findViewById<View>(R.id.nowPlayingView)
        nowPlayingView?.translationY = translation

        // Animate the rest of the way
        val endTranslation = if (dy > 0) child.height.toFloat() else 0f
        animateRemaining(child, endTranslation)
        nowPlayingView?.let { animateRemaining(it, endTranslation) }
    }

    private fun animateRemaining(view: View, endTranslation: Float) {
        ObjectAnimator.ofFloat(view, "translationY", endTranslation).apply {
            duration = ANIMATION_DURATION
            start()
        }
    }

    companion object {
        const val ANIMATION_DURATION = 300L
    }
}