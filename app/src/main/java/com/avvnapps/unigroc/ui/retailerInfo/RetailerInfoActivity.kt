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
import com.avvnapps.unigroc.utils.bindView
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_retailer_info.*

class RetailerInfoActivity : AppCompatActivity() {

    val TAG = "RETAILER_INFO"

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }
    var animationPlaybackSpeed: Double = 0.8

    private val recyclerView: RecyclerView by bindView(R.id.review_recyclerView)
    private val loadingDuration: Long
        get() = (resources.getInteger(R.integer.loadingAnimDuration) / animationPlaybackSpeed).toLong()


    var reviewList: List<Review> = emptyList()
    lateinit var reviewAdapter: ReviewAdapter

    private var retailerId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retailer_info)

        retailerId = intent.getStringExtra("retailerID")

        getRetailersDetail()

        initialiseFirestoreViewModel(retailerId)

        reviewAdapter = ReviewAdapter(this@RetailerInfoActivity, reviewList)

        review_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RetailerInfoActivity)
            adapter = reviewAdapter
            setHasFixedSize(true)
        }.also {
            updateRecyclerViewAnimDuration()
        }


    }

    private fun getRetailersDetail() {

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

    /**
     * Update RecyclerView Item Animation Durations
     */
    private fun updateRecyclerViewAnimDuration() = recyclerView.itemAnimator?.run {
        removeDuration = loadingDuration * 60 / 100
        addDuration = loadingDuration
    }
}