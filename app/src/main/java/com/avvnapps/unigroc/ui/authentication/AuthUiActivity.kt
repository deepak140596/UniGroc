package com.avvnapps.unigroc.ui.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.ui.MainActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase

class AuthUiActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1
    private val TAG = "AUTH_UI"
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        title = "Sign In"
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth!!.currentUser
        if (user == null)
            chooseAuthProviders()
        else
            startPhoneAuthActivity(user)
    }

    override fun onResume() {
        super.onResume()
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth!!.currentUser
        if (user == null)
            chooseAuthProviders()
        else
            startPhoneAuthActivity(user)
    }

    private var customLayout = AuthMethodPickerLayout.Builder(R.layout.auth_layout)
        .setGoogleButtonId(R.id.g_plus)
        .setEmailButtonId(R.id.emailAuth)
        .build()

    private fun chooseAuthProviders() {
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
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                FirebaseCrashlytics.getInstance().setCustomKey(TAG, response!!.error.toString())
                Log.e(TAG, "Sign In Failed. " + response.error)
            }
        }
    }


    private fun startPhoneAuthActivity(user: FirebaseUser? = null) {
        if (user != null) {
            Log.i(TAG, "Phone Number: ${user.phoneNumber}")
            //Toast.makeText(this,"Signed In",Toast.LENGTH_SHORT).show()
            if (user.phoneNumber == null || user.phoneNumber.toString().isEmpty())
                startActivity(Intent(this, VerifyPhoneActivity::class.java))
            else {
                val firestore = Firebase.firestore
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "getInstanceId failed", task.exception)
                            return@OnCompleteListener
                        }
                        // Get new Instance ID token
                        val token = task.result?.token.toString()
                        val data = hashMapOf("deviceToken" to token)

                        firestore.collection("users")
                            .document(user.email.toString())
                            .set(data, SetOptions.merge())
                        // Log and toast
                        //Log.d(TAG, "tokenID $token")
                        // already signed in
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    })
            }
        }
    }

}