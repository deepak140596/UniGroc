package com.avvnapps.unigroc.order_status

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.utils.PriceFormatter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.order_item.view.*

class ItemDetailAdapter(
    var context: Context,
    var cartList: List<CartEntity>
) : RecyclerView.Adapter<ItemDetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val cartItem = cartList[position]
        holder.bindItems(context, cartItem)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bindItems(context: Context, cartItem: CartEntity) {

            itemView.order_item_name_tv.text = cartItem.name
            itemView.order_item_metric_weight_tv.text = getQuantityAndWeight(cartItem)
            itemView.order_item_total_price_tv.text = PriceFormatter.getFormattedPrice(
                context,
                cartItem.price?.times(
                    cartItem.quantity!!
                )!!
            )
            itemView.order_item_price_tv.text =
                PriceFormatter.getFormattedPrice(context, cartItem.price!!)

            if (cartItem.photoUrl != null) {
                Glide.with(context).load(cartItem.photoUrl)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate().into(itemView.order_item_iv)
            }


        }

        private fun getQuantityAndWeight(cartItem: CartEntity): String {
            return "${cartItem.metricWeight} x ${cartItem.quantity}"
        }
    }


}