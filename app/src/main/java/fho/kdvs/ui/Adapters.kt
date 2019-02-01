package fho.kdvs.ui

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
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffCallback), ClickableRecyclerViewAdapter<T> {
    override var clickHandler: (ClickData<T>) -> Unit = {
        throw NotImplementedError("Should define clickHandler for this adapter")
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.apply { bind(makeOnClickListener(item), item) }
    }
}

data class ClickData<T>(val view: View, val item: T)

interface ClickableRecyclerViewAdapter<T> {
    var clickHandler: (ClickData<T>) -> Unit

    fun makeOnClickListener(item: T): View.OnClickListener {
        return View.OnClickListener { clickHandler(ClickData(it, item)) }
    }
}