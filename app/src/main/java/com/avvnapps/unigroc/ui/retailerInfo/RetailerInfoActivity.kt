package com.avvnapps.unigroc.ui.retailerInfo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.Review
import com.avvnapps.unigroc.utils.circularProgressDrawable
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.acount_info_layout.*
import kotlinx.android.synthetic.main.activity_retailer_info.*

class RetailerInfoActivity : AppCompatActivity() {

    val TAG by lazy { "RETAILER_INFO" }

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }

    private val firestoreDB by lazy { FirebaseFirestore.getInstance() }

    var reviewList: List<Review> = emptyList()
    lateinit var reviewAdapter: ReviewAdapter

    private var retailerId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retailer_info)

        retailerId = intent.getStringExtra("retailerID")

        getRetailersDetail()

        reviewAdapter = ReviewAdapter(this@RetailerInfoActivity, reviewList)

        review_recyclerView.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(this@RetailerInfoActivity, RecyclerView.VERTICAL, false)
            adapter = reviewAdapter

        }

    }

    private fun getRetailersDetail() {
        val docRef = firestoreDB.collection("retailers").document(retailerId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                initialiseFirestoreViewModel(retailerId)
                setRetailerData(snapshot)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun setRetailerData(retailerData: DocumentSnapshot?) {

        retailerData?.get("shopName")?.toString()?.let {
            retailerShopName.text = it
        }

        retailerData?.get("profilePic")?.toString()?.let {
            val circularProgressDrawable = circularProgressDrawable(this)

            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)

            Glide.with(applicationContext)
                .applyDefaultRequestOptions(options)
                .load(it)
                .into(retailerBackgroundImage)
        }

        retailerData?.get("shopName")?.toString()?.let {
            retailerShopName.text = it
        }
        retailerData?.get("storeAddress")?.toString()?.let {
            retailerAddress.text = it
        }

        retailerData?.get("rating")?.toString()?.let {
            RetailerRatingBar.rating = it.toFloat()
            retailerRating.text = it
        }
    }

    private fun initialiseFirestoreViewModel(retailerId: String?) {
        retailerId?.let {
            firestoreViewModel.getReviews(it)
                .observe(this@RetailerInfoActivity, Observer { orders ->
                    Log.i(TAG, "OrdersSize: ${orders.size}")
                    if (orders.isNullOrEmpty()) {
                        noOfReviews.text = "0"
                    } else {
                        noOfReviews.text = orders.size.toString()
                    }
                    reviewList = orders

                    reviewAdapter.reviewList = reviewList
                    reviewAdapter.notifyDataSetChanged()


                })
        }
    }


}