package com.avvnapps.unigroc.ui.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel

class RetailerInfoActivity : AppCompatActivity() {

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }

    private var retailerId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retailer_info)

        retailerId = intent.getStringExtra("retailerID")


    }
}