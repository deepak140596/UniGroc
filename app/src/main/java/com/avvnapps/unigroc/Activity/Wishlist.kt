package com.avvnapps.unigroc.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.avvnapps.unigroc.Adapter.wishlistAdapter
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.wishlistItems
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_wishlist.*
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QueryDocumentSnapshot





class Wishlist : AppCompatActivity() {

    lateinit var adapter: wishlistAdapter
    lateinit var wishlistItem: ArrayList<wishlistItems>

    private var mLayoutManager: StaggeredGridLayoutManager? = null
    var user = FirebaseAuth.getInstance().currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        val toolbar = findViewById(com.avvnapps.unigroc.R.id.toolbar_wishlist) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        wishlistItem = ArrayList()
        adapter = wishlistAdapter(this, wishlistItem)
        //using staggered grid pattern in recyclerview
        mLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerview.setHasFixedSize(true)
        recyclerview.setLayoutManager(mLayoutManager)
        val rootRef = FirebaseFirestore.getInstance()
        val query =
            rootRef!!.collection("users").document(user!!.email.toString()).collection("wishlist")

        query.addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                if (p1 != null) {
                    return
                }

                if (p0 != null) {
                    wishlistItem.clear()
                    for (documentSnap: QueryDocumentSnapshot in p0) {
                        if (tv_no_cards.getVisibility() == View.VISIBLE) {
                            tv_no_cards.setVisibility(View.GONE)
                        }
                        val getID = documentSnap.getId()
                        val wishlist = documentSnap!!.toObject(wishlistItems::class.java!!)
                        wishlistItem.add(wishlist)
                        adapter.notifyDataSetChanged()

                    }
                    if (wishlistItem.isEmpty()){
                        empty_cart.visibility = View.VISIBLE
                    }
                }
                recyclerview.adapter = adapter

            }

        })

    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}
