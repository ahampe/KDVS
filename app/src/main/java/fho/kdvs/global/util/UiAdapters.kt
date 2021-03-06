package fho.kdvs.global.util

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/** Base view holder used with [BindingRecyclerViewAdapter]. */
abstract class BindingViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(listener: View.OnClickListener, item: T)
}

/** [ListAdapter] subclass for use with view binding.  */
abstract class BindingRecyclerViewAdapter<T, VH : BindingViewHolder<T>>(
    onClick: (ClickData<T>) -> Unit,
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffCallback), ClickableRecyclerViewAdapter<T> {
    override val clickHandler: (ClickData<T>) -> Unit = onClick

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(makeOnClickListener(item), item)
    }
}

data class ClickData<T>(val view: View, val item: T)

interface ClickableRecyclerViewAdapter<T> {
    val clickHandler: (ClickData<T>) -> Unit

    fun makeOnClickListener(item: T): View.OnClickListener {
        return View.OnClickListener { clickHandler(ClickData(it, item)) }
    }
}