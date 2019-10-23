package com.avvnapps.unigroc.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.wishlistItems
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import es.dmoral.toasty.Toasty

class wishlistAdapter(var context: Context, var wishlistItems: ArrayList<wishlistItems>) :
    RecyclerView.Adapter<wishlistAdapter.wishlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): wishlistViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cart_item_layout, parent, false)
        return wishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: wishlistViewHolder, position: Int) {
        holder.bindItems(context, wishlistItems[position])
    }

    override fun getItemCount(): Int {
        return wishlistItems.size
    }


    inner class wishlistViewHolder internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {
        lateinit var context: Context

        fun bindItems(context: Context, wishlistItems: wishlistItems) {
            this.context = context

            cardname.setText(wishlistItems.name)

            if (wishlistItems.photoUrl != null) {
                Glide.with(context).load(wishlistItems.photoUrl)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate().into(cardimage)
            }

            cardprice.text = wishlistItems.price.toString()
            cardcount.text = wishlistItems.no_of_items.toString()
        }

        internal var cardname: TextView
        internal var cardimage: ImageView
        internal var cardprice: TextView
        internal var cardcount: TextView
        internal var carddelete: ImageView

        init {
            cardname = view.findViewById(R.id.cart_prtitle)
            cardimage = view.findViewById(R.id.image_cartlist)
            cardprice = view.findViewById(R.id.cart_price)
            cardcount = view.findViewById(R.id.cart_prcount)
            carddelete = view.findViewById(R.id.deletecard)
        }

    }

}