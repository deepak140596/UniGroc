package com.avvnapps.unigroc.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.avvnapps.unigroc.R
import android.content.Intent

import android.os.Handler
import android.view.Window
import android.view.WindowManager
import com.avvnapps.unigroc.MainActivity
import com.avvnapps.unigroc.authentication.AuthUiActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(
            Runnable /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */
            {
                // This method will be executed once the timer is over
                // Start your app main activity
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser != null) {
                    // already signed in
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))

                } else {
                    // not signed in
                    startActivity(Intent(this@SplashActivity, AuthUiActivity::class.java))

                }
                finish()
            }, 2500
        )
    }
}
