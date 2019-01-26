package fho.kdvs.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import fho.kdvs.R
import fho.kdvs.model.database.entities.ShowEntity
import kotlinx.android.synthetic.main.fragment_schedule.*
import kotlinx.android.synthetic.main.layout_schedule_item.view.*
import java.util.*

class ScheduleFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: MyPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = rv_sun as RecyclerView
        recyclerView.apply{
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewPager = pager as ViewPager
        viewPager.apply{
            viewPagerAdapter = MyPagerAdapter(context)
            //TODO: swipe listener
        }
    }
}

class MyAdapter(private val showDataset: List<ShowEntity>):
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_schedule_item, parent, false) as CardView
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageView = holder.cardView.show_img
        Glide.with(holder.cardView)
            .load(showDataset[position].defaultImageHref)
            .into(imageView)
        holder.cardView.show_name.text = showDataset[position].name
        holder.cardView.show_time.text = getStringFromTimes(showDataset[position].timeStart,
                                                            showDataset[position].timeEnd)
    }

    override fun getItemCount() = showDataset.size

    companion object {
        fun getStringFromTimes(timeStart: Date?, timeEnd: Date?) : String?{
            val calStart = Calendar.getInstance(); calStart.time = timeStart
            val calEnd = Calendar.getInstance(); calEnd.time = timeEnd

            val hourStart = calStart.get(Calendar.HOUR)
            val minuteStart = calStart.get(Calendar.MINUTE)
            val isPMStart = hourStart > 12

            val hourEnd = calEnd.get(Calendar.HOUR)
            val minuteEnd = calEnd.get(Calendar.MINUTE)
            val isPMEnd = hourEnd > 12
            
            return (hourStart % 12).toString() + ":" +
                    minuteStart.toString() + 
                    (if (isPMStart) "PM" else "AM" ) + " - " +
                    (hourEnd % 12).toString() + ":" +
                    minuteEnd.toString() +
                    (if (isPMEnd) "PM" else "AM" )
        }
    }
}

class MyPagerAdapter(private var mContext: Context) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val myPagerEnum = MyPagerEnum.values()[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(myPagerEnum.getLayoutResId(), collection, false) as ViewGroup
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return MyPagerEnum.values().size
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val myPagerEnum = MyPagerEnum.values()[position]
        return mContext.getString(myPagerEnum.getTitleResId())
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    fun getView(position: Int): String {
        val myPagerEnum = MyPagerEnum.values()[position]
        return mContext.getString(myPagerEnum.getLayoutResId())
    }

    fun getPrevView(position: Int): String {
        val myPagerEnum = MyPagerEnum.values()[(position - 1) % count]
        return mContext.getString(myPagerEnum.getLayoutResId())
    }

    fun getNextView(position: Int): String {
        val myPagerEnum = MyPagerEnum.values()[(position + 1) % count]
        return mContext.getString(myPagerEnum.getLayoutResId())
    }
}

enum class MyPagerEnum(private var mTitleResId: Int, private var mLayoutResId: Int) {
    SUNDAY(R.string.sun, R.id.rv_sun),
    MONDAY(R.string.mon, R.id.rv_mon),
    TUESDAY(R.string.tues, R.id.rv_tues),
    WEDNESDAY(R.string.wed, R.id.rv_wed),
    THURSDAY(R.string.thurs, R.id.rv_thurs),
    FRIDAY(R.string.fri, R.id.rv_fri),
    SATURDAY(R.string.sat, R.id.rv_sat);

    fun getTitleResId(): Int {
        return mTitleResId
    }

    fun getLayoutResId(): Int {
        return mLayoutResId
    }
}
