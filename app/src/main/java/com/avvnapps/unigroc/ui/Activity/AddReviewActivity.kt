package com.avvnapps.unigroc.ui.Activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.Review
import com.avvnapps.unigroc.utils.hide
import com.avvnapps.unigroc.utils.show
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_add_review.*


class AddReviewActivity : AppCompatActivity() {

    val TAG by lazy { "AddReviewActivity" }
    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }

    val user by lazy { FirebaseAuth.getInstance().currentUser }

    val firestore by lazy { Firebase.firestore }

    private var retailerId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        retailerId = intent.getStringExtra("retailerName")

        Log.e(TAG, retailerId)

        close_add_review_activity.setOnClickListener {
            onBackPressed()
        }

        RatingBar.setOnRatingChangeListener { ratingBar, rating, fromUser ->
            ratingBar.setMinimumStars(1.0F)
            imageReviewRL.hide()
            submitDeliveryReviewButton.show()
            ReviewLayoutRl.show()
            Log.e(TAG, "onRatingChange: $rating")
        }

        setupOnclick()

    }

    private fun setupOnclick() {
        submitDeliveryReviewButton.setOnClickListener {
            if (isFormValid()) {
                activity_review_order_progress_bar.show()
                submitDeliveryReviewButton.isEnabled = false
                val reviewId = firestore.collection("retailers").document(retailerId)
                    .collection("reviews").document().id
                val reviewData = Review(
                    reviewId,
                    RatingBar.rating,
                    user?.displayName.toString(),
                    user?.photoUrl.toString(),
                    whatDidYouLikeEditText.text.toString().trim(),
                    whatWasNotMarkEditText.text.toString().trim(),
                    writeReviewEditText.text.toString().trim()
                )

                firestore.collection("retailers").document(user?.email.toString())
                    .collection("reviews").document(reviewId)
                    .set(reviewData)
                    .addOnSuccessListener {
                        activity_review_order_progress_bar.hide()
                        submitDeliveryReviewButton.isEnabled = true
                        onBackPressed()
                        Toasty.success(this, "Thank You for your review").show()
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }


            }
        }
    }

    private fun isFormValid(): Boolean {
        if (whatDidYouLikeEditText.text.toString().trim()
                .isEmpty() and whatWasNotMarkEditText.text.toString().trim()
                .isEmpty() and writeReviewEditText.text.toString().trim().isEmpty()
        ) {
            Toasty.warning(this, "Can not be empty").show()
            return false
        }

        return true
    }


}


