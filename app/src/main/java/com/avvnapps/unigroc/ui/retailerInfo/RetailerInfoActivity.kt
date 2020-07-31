package com.avvnapps.unigroc.ui.retailerInfo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.Review
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_retailer_info.*

class RetailerInfoActivity : AppCompatActivity() {

    val TAG = "RETAILER_INFO"

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }


    var reviewList: List<Review> = emptyList()
    lateinit var reviewAdapter: ReviewAdapter

    private var retailerId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retailer_info)

        retailerId = intent.getStringExtra("retailerID")

        initialiseFirestoreViewModel(retailerId)

        reviewAdapter = ReviewAdapter(this@RetailerInfoActivity, reviewList)

        review_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RetailerInfoActivity)
            adapter = reviewAdapter
        }


    }

    private fun initialiseFirestoreViewModel(retailerId: String?) {
        retailerId?.let {
            firestoreViewModel.getReviews(it)
                .observe(this@RetailerInfoActivity, Observer { orders ->
                    Log.i(TAG, "OrdersSize: ${orders.size}")
                    reviewList = orders

                    reviewAdapter.reviewList = reviewList
                    reviewAdapter.notifyDataSetChanged()


                })
        }

    }
}