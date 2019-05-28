package com.avvnapps.unigroc.generate_cart

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.utils.PriceFormatter
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_cart.view.*

class CartItemAdapter(var context: Context, var cartList: List<CartEntity>,
                      var cartViewModel: CartViewModel)
    : RecyclerView.Adapter<CartItemAdapter.ViewHolder>(){
    var TAG = "CART_ITEM_ADAPTER"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartList[position]
        holder.bindItems(context,cartItem,cartViewModel)
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var TAG = "CART_ITEM_ADAPTER"

        fun bindItems(context: Context, cartItem: CartEntity, cartViewModel: CartViewModel){
            itemView.item_cart_name_tv.text = cartItem.name
            itemView.item_cart_metric_weight_tv.text = cartItem.metricWeight
            //itemView.item_cart_quantity_tv.text = cartItem.quantity.toString()
            if(cartItem.price == 0.0)
                itemView.item_cart_price_tv.visibility = View.GONE
            else
                itemView.item_cart_price_tv.text = PriceFormatter.getFormattedPrice(cartItem.price)

            if(cartItem.photoUrl != null){
                Glide.with(context).load(cartItem.photoUrl).into(itemView.item_cart_iv)
            }

            cartItem.quantity = cartViewModel.getQuantity(cartItem.itemId)
            Log.i(TAG,"Qty: ${cartItem.quantity}")
            if(cartItem.quantity == null)
                cartItem.quantity = 0
            updateViews(cartItem)

            /*if(cartItem.quantity == 0){
                itemView.item_cart_add_large_btn.visibility = View.VISIBLE
                itemView.item_cart_subtract_btn.visibility = View.INVISIBLE
            }
            else{
                itemView.item_cart_add_large_btn.visibility = View.GONE
                itemView.item_cart_subtract_btn.visibility = View.VISIBLE
            }*/

            itemView.item_cart_add_btn.setOnClickListener {
                cartItem.incrementQuantity()
                if(cartItem.quantity==1){
                    cartViewModel.insert(cartItem)
                }else {
                    cartViewModel.setQuantity(cartItem.itemId,cartItem.quantity)
                }
                updateViews(cartItem)

            }
            itemView.item_cart_add_large_btn.setOnClickListener {

                cartItem.incrementQuantity()
                cartViewModel.insert( cartItem)
                updateViews(cartItem)
            }
            itemView.item_cart_subtract_btn.setOnClickListener {
                cartItem.decrementQuantity()
                if(cartItem.quantity == 0){
                    cartViewModel.delete(cartItem)
                }else{
                    cartViewModel.setQuantity(cartItem.itemId,cartItem.quantity)
                }

                updateViews(cartItem)
            }

        }
        fun updateViews(cartItem: CartEntity){
            itemView.item_cart_quantity_tv.text = cartItem.quantity.toString()
            if(cartItem.quantity == 0){
                itemView.item_cart_add_large_btn.visibility = View.VISIBLE
                itemView.item_cart_subtract_btn.visibility = View.INVISIBLE
            }
            else{
                itemView.item_cart_add_large_btn.visibility = View.GONE
                itemView.item_cart_subtract_btn.visibility = View.VISIBLE
            }
        }

    }
}