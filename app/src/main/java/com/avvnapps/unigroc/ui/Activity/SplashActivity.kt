package com.avvnapps.unigroc.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.ui.authentication.AuthUiActivity


class SplashActivity : AppCompatActivity() {
    val TAG = "SPLASH_SCREEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(
            Runnable /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */
            {
                // This method will be executed once the timer is over
                // Start your app main activity
                startActivity(Intent(this@SplashActivity, AuthUiActivity::class.java))
                finish()
            }, 2500
        )
    }
}
