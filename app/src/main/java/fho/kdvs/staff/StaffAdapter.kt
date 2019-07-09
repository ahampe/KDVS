package fho.kdvs.staff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellStaffBinding
import fho.kdvs.global.database.StaffEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.home.StaffDiffCallback

class StaffAdapter(onClick: (ClickData<StaffEntity>) -> Unit) :
    BindingRecyclerViewAdapter<StaffEntity, BindingViewHolder<StaffEntity>>(onClick, StaffDiffCallback()){

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<StaffEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellStaffBinding.inflate(inflater, parent, false)
        return StaffViewHolder(binding)
    }

    class StaffViewHolder(private val binding: CellStaffBinding) :
        BindingViewHolder<StaffEntity>(binding.root){
        override fun bind(listener: View.OnClickListener, item: StaffEntity) {
            binding.apply {
                staff = item
                clickListener = listener
            }
        }
    }

    fun onStaffChanged(staff: List<StaffEntity>) {
        submitList(staff)
    }
}