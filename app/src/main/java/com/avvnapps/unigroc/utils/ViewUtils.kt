package com.avvnapps.unigroc.utils

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.avvnapps.unigroc.BuildConfig
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing =
    throw IllegalStateException("View ID $id for '${desc.name}' not found.")


@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(
    id: Int,
    finder: T.(Int) -> View?,
    onInitializedListener: ((V) -> Unit)? = null
) = Lazy { t: T, desc ->
    t.finder(id) as V? ?: viewNotFound(id, desc)
}.apply { this.onInitializedListener = onInitializedListener }


// Like Kotlin's lazy delegate but the initializer gets the target and metadata passed to it
private class Lazy<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V>,
    LifecycleObserver {

    var onInitializedListener: ((V) -> Unit)? = null

    private object EMPTY

    private var value: Any? = EMPTY
    private var attachedToLifecycleOwner = false

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        checkAddToLifecycleOwner(thisRef)
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        val v = value as V

        onInitializedListener?.invoke(v)
        return v
    }

    private fun checkAddToLifecycleOwner(thisRef: T) {
        if (!attachedToLifecycleOwner && thisRef is LifecycleOwner) {
            thisRef.lifecycle.addObserver(this)
            attachedToLifecycleOwner = true
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun destroy() {
        value = EMPTY
    }
}

private val RecyclerView.ViewHolder.viewFinder: RecyclerView.ViewHolder.(Int) -> View?
    get() = { itemView.findViewById(it) }

private val Activity.viewFinder: Activity.(Int) -> View?
    get() = { findViewById(it) }

fun <V : View> Activity.bindView(id: Int)
        : ReadOnlyProperty<Activity, V> = required(id, viewFinder)

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

fun Any.bindColor(context: Context, @ColorRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    ContextCompat.getColor(context, id)
}

fun Any.bindDimen(context: Context, @DimenRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    context.resources.getDimension(id)
}

fun <V : View> RecyclerView.ViewHolder.bindView(id: Int)
        : ReadOnlyProperty<RecyclerView.ViewHolder, V> = required(id, viewFinder)


inline val Context.screenWidth: Int
    get() = Point().also {
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(
            it
        )
    }.x
inline val View.screenWidth: Int
    get() = context!!.screenWidth

inline val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()
inline val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )


inline fun getValueAnimator(
    forward: Boolean = true,
    duration: Long,
    interpolator: TimeInterpolator,
    crossinline updateListener: (progress: Float) -> Unit
): ValueAnimator {
    val a =
        if (forward) ValueAnimator.ofFloat(0f, 1f)
        else ValueAnimator.ofFloat(1f, 0f)
    a.addUpdateListener { updateListener(it.animatedValue as Float) }
    a.duration = duration
    a.interpolator = interpolator
    return a
}

fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
    val inverseRatio = 1f - ratio

    val a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio)
    val r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio)
    val g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio)
    val b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio)
    return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
}


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