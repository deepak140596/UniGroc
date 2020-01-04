package com.avvnapps.unigroc.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.avvnapps.unigroc.NavigationActivity
import com.avvnapps.unigroc.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import com.firebase.ui.auth.AuthMethodPickerLayout
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.avvnapps.unigroc.MainActivity


/**
 * Created by Deepak Prasad on 11-02-2019.
 */
class AuthUiActivity : AppCompatActivity() {

    val RC_SIGN_IN = 1
    val TAG = "AUTH_UI"
    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Fabric.with(this, Crashlytics())
        title = "Sign In"

        mAuth = FirebaseAuth.getInstance()

        //signOut()

    }

    override fun onResume() {
        super.onResume()
        mAuth = FirebaseAuth.getInstance()
        var user = mAuth!!.currentUser
        if (user == null)
            chooseAuthProviders()
        else
            startPhoneAuthActivity(user)
    }

    var customLayout = AuthMethodPickerLayout.Builder(R.layout.auth_layout)
    .setGoogleButtonId(R.id.g_plus)
    .setEmailButtonId(R.id.emailAuth)
    .build()

    fun chooseAuthProviders() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.RedTheme)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            RC_SIGN_IN
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                startPhoneAuthActivity(user)
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Log.e(TAG, "Sign In Failed. " + response!!.error)
            }
        }
    }


    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // ...
            }
    }

    fun startPhoneAuthActivity(user: FirebaseUser? = null) {
        if (user != null) {
            Log.i(TAG, "Phone Number: ${user.phoneNumber}")
            //Toast.makeText(this,"Signed In",Toast.LENGTH_SHORT).show()
            if (user.phoneNumber == null || user.phoneNumber.toString().length == 0)
                startActivity(Intent(this, VerifyPhoneActivity::class.java))
            else
                startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}