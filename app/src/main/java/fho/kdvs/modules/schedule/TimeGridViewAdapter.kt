package fho.kdvs.modules.schedule

import android.view.LayoutInflater
import android.view.View
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

    private val numBlocks = 24

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val timeContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_timeblock, parent, false) as ConstraintLayout
        return ViewHolder(timeContainer)
    }

    override fun getItemCount(): Int = numBlocks

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = fragment.getString(
            R.string.hourLabel,
            position + 1
        )
        if (position == numBlocks - 1) {
            holder.itemView.hourLabel.visibility = View.INVISIBLE
            holder.itemView.hourMark.visibility = View.GONE
        }
    }

    class ViewHolder(timeContainer: ConstraintLayout) : RecyclerView.ViewHolder(timeContainer) {
        val textView: TextView = timeContainer.hourLabel
    }
}