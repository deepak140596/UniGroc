package com.avvnapps.unigroc.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avvnapps.unigroc.Adapter.wishlistAdapter
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.wishlistItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_wishlist.*


class Wishlist : AppCompatActivity() {

    lateinit var adapter: wishlistAdapter
    lateinit var wishlistItem: ArrayList<wishlistItems>

    private var mLayoutManager: StaggeredGridLayoutManager? = null
    var user = FirebaseAuth.getInstance().currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        val toolbar = findViewById<Toolbar>(com.avvnapps.unigroc.R.id.toolbar_wishlist)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        wishlistItem = ArrayList()
        adapter = wishlistAdapter(this, wishlistItem)
        //using staggered grid pattern in recyclerview
        mLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = mLayoutManager

        view_profile.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        })
        //fetch data from wishlist
        popuateData()


    }

    private fun popuateData() {
        val rootRef = FirebaseFirestore.getInstance()
        val query =
            rootRef.collection("users").document(user!!.email.toString()).collection("wishlist")

        query.addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                if (p1 != null) {
                    return
                }

                if (p0 != null) {
                    wishlistItem.clear()
                    for (documentSnap: QueryDocumentSnapshot in p0) {
                        val wishlist = documentSnap.toObject(wishlistItems::class.java)
                        wishlistItem.add(wishlist)
                        tv_no_cards.visibility = View.GONE
                        adapter.notifyDataSetChanged()

                    }
                    if (wishlistItem.isEmpty()){
                        tv_no_cards.visibility = View.GONE
                        empty_cart.visibility = View.VISIBLE
                    }
                }
                recyclerview.adapter = adapter

            }

        })    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}
