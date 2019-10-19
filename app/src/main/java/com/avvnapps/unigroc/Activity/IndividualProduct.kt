package com.avvnapps.unigroc.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.activity_individual_product.*

class IndividualProduct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_product)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val cartItem = intent.getParcelableExtra("product") as CartEntity

        init(cartItem)
    }

    private fun init(cartItem: CartEntity) {
        if (cartItem!!.photoUrl != null) {
            Glide.with(this).load(cartItem!!.photoUrl)
                .transition(withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate().into(productimage)
        }

        product_name.setText(cartItem!!.name)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
