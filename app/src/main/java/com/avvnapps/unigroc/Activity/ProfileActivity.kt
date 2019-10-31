package com.avvnapps.unigroc.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.avvnapps.unigroc.MainActivity
import com.avvnapps.unigroc.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    var TAG = "PROFILE_ACTIVITY"
    var user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val toolbar = findViewById(R.id.profile_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        Log.i(
            TAG,
            "Name: ${user!!.displayName}  Email: ${user!!.email}  Phone: ${user!!.phoneNumber}"
        )

        init()

    }

    private fun init(){
        name_tv.setText(user!!.displayName)
        emailview.setText(user!!.email)
        mobileview.setText(user!!.phoneNumber)
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
        return true
    }
}
