package com.avvnapps.unigroc.utils

import android.animation.Animator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.avvnapps.unigroc.BuildConfig
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty

fun View.snackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

fun Context.successToast(message: String) {
    Toasty.success(this, message).show()
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.getColorRes(@ColorRes id: Int) = ContextCompat.getColor(applicationContext, id)

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun circularProgressDrawable(context: Context): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()
    return circularProgressDrawable
}

fun Activity.openAppInGooglePlay() {
    val appId = BuildConfig.APPLICATION_ID
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appId")
            ).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
            })

    } catch (anfe: ActivityNotFoundException) {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appId")
            )
        )
    }
}


fun View.animateVisibility(visible: Int) {
    if (visible == View.VISIBLE) {
        visibility = View.VISIBLE
        alpha = 0f
        scaleX = 0f
        scaleY = 0f
    }
    val value = if (visible == View.VISIBLE) 1f else 0f
    animate()
        .alpha(value)
        .scaleX(value)
        .scaleY(value)
        .setDuration(300)
        .setInterpolator(OvershootInterpolator())
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if (visible == View.GONE)
                    visibility = View.GONE
                else
                    animate()
                        .setInterpolator(LinearOutSlowInInterpolator())
                        .start()
            }

            override fun onAnimationCancel(animation: Animator) {
                if (visible == View.GONE) {
                    visibility = View.GONE
                }
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        .start()
}