package com.avvnapps.unigroc.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun Context.SuccessToast(message: String) {
    Toasty.success(this, message).show()
}