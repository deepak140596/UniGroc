package com.avvnapps.unigroc.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.avvnapps.unigroc.R
import android.content.Intent
import androidx.core.os.HandlerCompat.postDelayed
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Handler
import com.avvnapps.unigroc.MainActivity
import com.avvnapps.unigroc.authentication.AuthUiActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.avvnapps.unigroc.R.layout.activity_splash)

        Handler().postDelayed(
            Runnable /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */
            {
                // This method will be executed once the timer is over
                // Start your app main activity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, 2500
        )
    }
}
