package com.avvnapps.unigroc.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.avvnapps.unigroc.MainActivity
import com.avvnapps.unigroc.R
import kotlinx.android.synthetic.main.activity_empty_cart_.*




class EmptyCart : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty_cart_)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        supportActionBar!!.setTitle("Cart")

        btn_shopnow.setOnClickListener {
           onBackPressed()
        }


    }
}
