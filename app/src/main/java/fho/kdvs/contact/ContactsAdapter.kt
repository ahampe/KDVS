package fho.kdvs.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fho.kdvs.databinding.CellContactBinding
import fho.kdvs.global.database.ContactEntity
import fho.kdvs.global.util.BindingRecyclerViewAdapter
import fho.kdvs.global.util.BindingViewHolder
import fho.kdvs.global.util.ClickData
import fho.kdvs.home.ContactDiffCallback

class ContactsAdapter(onClick: (ClickData<ContactEntity>) -> Unit) :
    BindingRecyclerViewAdapter<ContactEntity, BindingViewHolder<ContactEntity>>(onClick, ContactDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<ContactEntity> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = fho.kdvs.databinding.CellContactBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    class ContactViewHolder(private val binding: CellContactBinding) : BindingViewHolder<ContactEntity>(binding.root){
        override fun bind(listener: View.OnClickListener, item: ContactEntity) {
            binding.apply {
                contact = item
            }
        }
    }

    fun onContactsChanged(contacts: List<ContactEntity>) {
        submitList(contacts)
    }
}