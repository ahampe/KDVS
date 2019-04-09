package fho.kdvs.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellContactBinding
import fho.kdvs.global.database.StaffEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.home.ContactDiffCallback

class ContactsAdapter(onClick: (ClickData<StaffEntity>) -> Unit) :
    BindingRecyclerViewAdapter<StaffEntity, BindingViewHolder<StaffEntity>>(onClick, ContactDiffCallback()){

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<StaffEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = fho.kdvs.databinding.CellContactBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    class ContactViewHolder(private val binding: CellContactBinding) : BindingViewHolder<StaffEntity>(binding.root){
        override fun bind(listener: View.OnClickListener, item: StaffEntity) {
            binding.apply {
                contact = item
            }
        }
    }

    fun onContactsChanged(staff: List<StaffEntity>) {
        submitList(staff)
    }
}