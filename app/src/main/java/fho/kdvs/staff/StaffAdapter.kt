package fho.kdvs.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellStaffBinding
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.StaffEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.home.StaffDiffCallback

class StaffAdapter(private val viewModel: SharedViewModel, onClick: (ClickData<StaffEntity>) -> Unit) :
    BindingRecyclerViewAdapter<StaffEntity, BindingViewHolder<StaffEntity>>(onClick, StaffDiffCallback()){

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<StaffEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = fho.kdvs.databinding.CellStaffBinding.inflate(inflater, parent, false)
        return StaffViewHolder(binding, viewModel)
    }

    class StaffViewHolder(private val binding: CellStaffBinding, private val viewModel: SharedViewModel) :
        BindingViewHolder<StaffEntity>(binding.root){
        override fun bind(listener: View.OnClickListener, item: StaffEntity) {
            binding.apply {
                staff = item
                vm = viewModel
            }
        }
    }

    fun onStaffChanged(staff: List<StaffEntity>) {
        submitList(staff)
    }
}