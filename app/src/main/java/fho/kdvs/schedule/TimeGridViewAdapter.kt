package fho.kdvs.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import fho.kdvs.R
import kotlinx.android.synthetic.main.cell_timeblock.view.*

/** A [RecyclerView.Adapter] for timeblock cells. */
class TimeGridViewAdapter(
    private val fragment: ScheduleFragment
) : RecyclerView.Adapter<TimeGridViewAdapter.ViewHolder>() {

    // Simple flag for scrolling to current show view. This will only be done once, after the fragment is created.
    //private var scrollingToCurrentShow = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val timeContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_timeblock, parent, false) as ConstraintLayout
        return ViewHolder(timeContainer)
    }

    override fun getItemCount(): Int = 23 // no need for final hour label

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = fragment.getString(
            R.string.hourLabel,
            position + 1
        )
    }

    class ViewHolder(timeContainer: ConstraintLayout) : RecyclerView.ViewHolder(timeContainer) {
        val textView: TextView = timeContainer.hourLabel
    }
}