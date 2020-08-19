package com.avvnapps.unigroc.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.ui.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.location_range_bottomsheet.view.*
import java.text.NumberFormat

class ProfileActivity : AppCompatActivity() {
    var TAG = "PROFILE_ACTIVITY"
    val user by lazy { FirebaseAuth.getInstance().currentUser }
    private val cartViewModel by lazy {
        ViewModelProvider(this).get(CartViewModel::class.java)
    }

    private var savedCartItems: List<CartEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val toolbar = findViewById<Toolbar>(R.id.profile_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            updateCartImageView(savedCartItems)
        })

        init()

    }

    private fun updateCartImageView(savedCartItems: List<CartEntity>) {
        if (savedCartItems.isNotEmpty()) {
            appbar_dashboard_cart_no_tv.text = savedCartItems.size.toString()
            appbar_dashboard_cart_no_tv.visibility = View.VISIBLE
        } else
            appbar_dashboard_cart_no_tv.visibility = View.GONE
    }

    private fun init() {
        name_tv.text = user!!.displayName
        emailview.text = user!!.email
        mobileview.text = user!!.phoneNumber

        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.user)
            .error(R.drawable.user)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .dontAnimate()
            .dontTransform()

        Glide.with(this)
            .applyDefaultRequestOptions(options)
            .load(user!!.photoUrl)
            .into(profile_pic)

        change_pickUp_range.setOnClickListener {
            setLocationRange()
        }

        profile_appbar_dashboard_cart_rl.setOnClickListener {
            startActivity(Intent(this, ReviewCartActivity::class.java))

        }

        wishList_Button.setOnClickListener {
            startActivity(Intent(this, Wishlist::class.java))
        }
    }


    private fun setLocationRange() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.location_range_bottomsheet, null)

        val minDistl = SharedPreferencesDB.getLocationRange(this)
        if (minDistl == -1) {
        } else {
            view.sliderLocation.value = minDistl.toFloat()
            val format = NumberFormat.getInstance()
            format.maximumFractionDigits = 0
            view.locationRangeTv.text =
                "Delivery Range : ${format.format(minDistl)} km"
        }

        view.sliderLocation.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
                val format = NumberFormat.getInstance()
                format.maximumFractionDigits = 0
                view.locationRangeTv.text =
                    "PickUp Range : ${format.format(slider.value.toDouble())} km"
                SharedPreferencesDB.saveLocationRange(
                    this@ProfileActivity,
                    format.format(slider.value.toDouble()).toDouble()
                )

            }
        })

        view.sliderLocation.setLabelFormatter { value: Float ->
            val format = NumberFormat.getInstance()
            format.maximumFractionDigits = 0
            format.format(value.toDouble())
        }

        view.sliderLocation.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
            val format = NumberFormat.getInstance()
            format.maximumFractionDigits = 0
            view.locationRangeTv.text = "PickUp Range ${format.format(value.toDouble())}km"
        }


        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }
}
