package com.avvnapps.unigroc.location_address

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.item_address.view.*

class AddressItemAdapter(var context: Context, var addressList : List<AddressItem>, var firestoreViewModel: FirestoreViewModel )
    :RecyclerView.Adapter<AddressItemAdapter.ViewHolder>(){

    var TAG = "ADDRESS_ITEM_ADAPTER"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address,parent,false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val addressItem = addressList[position]
        holder.bindItems(context,addressItem,firestoreViewModel)
    }


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var TAG = "ADDRESS_ITEM_ADAPTER"

        fun bindItems(context: Context, addressItem : AddressItem, firestoreViewModel: FirestoreViewModel){
            itemView.item_saved_add_title.text = addressItem.addressName
            itemView.item_saved_add_body.text = addressItem.getAddress()

            itemView.item_address_options_tv.setOnClickListener { view ->
                var popupMenu = PopupMenu(context, itemView.item_address_options_tv)
                popupMenu.inflate(R.menu.menu_item_address)

                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){

                        R.id.menu_item_add_edit -> {
                            Log.i(TAG, "EDIT")
                            return@setOnMenuItemClickListener true
                        }

                        R.id.menu_item_add_delete -> {
                            Log.i(TAG, "EDIT")
                            return@setOnMenuItemClickListener true
                        }
                    }
                    return@setOnMenuItemClickListener  true

                }
            }


        }
    }
}