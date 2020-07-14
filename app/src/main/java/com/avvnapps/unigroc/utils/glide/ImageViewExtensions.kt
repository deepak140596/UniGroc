package com.avvnapps.unigroc.utils.glide

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.utils.GlideApp
import com.avvnapps.unigroc.utils.GlideRequest
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


fun ImageView.loadImage(
    imgUrl: String,
    scale: Boolean = false,
    callback: (loaded: Boolean) -> Unit = { }
) {
    if (imgUrl.isNotEmpty()) {
        createGlideRequest(Uri.parse(imgUrl), context)
            .listener(SvgSoftwareLayerSetter())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    callback(false)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    callback(true)
                    return false
                }
            })
            .error(createGlideRequest(Uri.parse(imgUrl), context, scale))
            .into(this).waitForLayout()
    }
}


fun createGlideRequest(
    source: Uri?,
    context: Context,
    resize: Boolean = false
): GlideRequest<Drawable> {
    val req = GlideApp.with(context)
        .load(source)
        .error(R.drawable.empty_cart)
    if (resize)
        req.centerCrop()
    else
        req.optionalFitCenter()
    return req
}
