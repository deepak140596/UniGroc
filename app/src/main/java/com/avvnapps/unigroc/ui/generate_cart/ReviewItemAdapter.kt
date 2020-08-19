package com.avvnapps.unigroc.ui.generate_cart

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.utils.PriceFormatter
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.top_selling_item_cart.view.*

class ReviewItemAdapter(var context: Context, var cartList: List<CartEntity>,
                        var cartViewModel: CartViewModel
) : RecyclerView.Adapter<ReviewItemAdapter.ReviewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {



        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.item_cart, parent, false)

        return ReviewHolder(itemView)
    }


    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        val cartItem = cartList[position]

        holder.bindItems(context, cartItem, cartViewModel)
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }

    fun getItem(position: Int): Any {
        return cartList[position]
    }


    inner class ReviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(p0: View?) {
            if (p0 != null) {
                mClickListener.onClick(adapterPosition, p0)
            }
        }

        var TAG = "CART_ITEM_ADAPTER"
        init {
            itemView.setOnClickListener(this)
        }
        fun bindItems(context: Context, cartItem: CartEntity, cartViewModel: CartViewModel) {
            itemView.item_cart_name_tv.text = cartItem.name
            itemView.item_cart_metric_weight_tv.text = cartItem.metricWeight
            //itemView.item_cart_quantity_tv.text = cartItem.quantity.toString()
            if (cartItem.price == 0.0)
                itemView.item_cart_price_tv.visibility = View.GONE
            else
                itemView.item_cart_price_tv.text = cartItem.price?.let {
                    PriceFormatter.getFormattedPrice(
                        context, it
                    )
                }


            if (cartItem.photoUrl != null) {
                Glide.with(context).load(cartItem.photoUrl)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate().into(itemView.item_cart_iv)
            }

            cartItem.quantity = cartViewModel.getQuantity(cartItem.itemId)
            Log.i(TAG, "Qty: ${cartItem.quantity}")
            if (cartItem.quantity == null)
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
                if (cartItem.quantity == 1) {
                    cartViewModel.insert(cartItem)
                } else {
                    cartViewModel.setQuantity(cartItem.itemId, cartItem.quantity!!)
                }
                updateViews(cartItem)

            }
            itemView.item_cart_add_large_btn.setOnClickListener {

                cartItem.incrementQuantity()
                cartViewModel.insert(cartItem)
                updateViews(cartItem)
            }
            itemView.item_cart_subtract_btn.setOnClickListener {
                cartItem.decrementQuantity()
                if (cartItem.quantity == 0) {
                    cartViewModel.delete(cartItem)
                } else {
                    cartViewModel.setQuantity(cartItem.itemId, cartItem.quantity!!)
                }

                updateViews(cartItem)
            }

        }

        private fun updateViews(cartItem: CartEntity) {
            itemView.item_cart_quantity_tv.text = cartItem.quantity.toString()
            if (cartItem.quantity == 0) {
                itemView.item_cart_add_large_btn.visibility = View.VISIBLE
                itemView.item_cart_subtract_btn.visibility = View.INVISIBLE
            } else {
                itemView.item_cart_add_large_btn.visibility = View.GONE
                itemView.item_cart_subtract_btn.visibility = View.VISIBLE
            }
        }

    }



}