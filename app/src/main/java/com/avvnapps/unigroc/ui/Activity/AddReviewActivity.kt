package com.avvnapps.unigroc.ui.Activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.utils.hide
import kotlinx.android.synthetic.main.activity_add_review.*


class AddReviewActivity : AppCompatActivity() {

    val TAG by lazy { "AddReviewActivity" }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)
        close_add_review_activity.setOnClickListener {
            onBackPressed()
        }

        RatingBar.setOnRatingChangeListener { ratingBar, rating, fromUser ->
            ratingBar.setMinimumStars(1.0F)
            imageReviewRL.hide()
            Log.e(TAG, "onRatingChange: $rating")
        }

    }


}


