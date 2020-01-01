package com.avvnapps.unigroc.Adapter

import android.content.Context
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class wishlistAdapter(var context: Context, var wishlistItems: ArrayList<wishlistItems>) :
    RecyclerView.Adapter<wishlistAdapter.wishlistViewHolder>() {
    var user = FirebaseAuth.getInstance().currentUser

    val rootRef = FirebaseFirestore.getInstance()
    val query =
        rootRef!!.collection("users").document(user!!.email.toString()).collection("wishlist")
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
            carddelete.setOnClickListener(View.OnClickListener {
                query.whereEqualTo("itemId", wishlistItems.itemId).get()
                    .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("wishData", "${document.id} => ${document.data}")
                    query.document(document.id)
                        .delete()
                }
            }
                .addOnFailureListener { exception ->
                    Log.w("wishData", "Error getting documents: ", exception)
                }


            })
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