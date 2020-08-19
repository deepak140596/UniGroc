package com.avvnapps.unigroc.Fonts

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.Nullable
import java.util.*

internal object FontCacheRagular {
    private val fontCache = HashMap<String, Typeface>()

    @Nullable
    fun getTypeface(fontname: String, context: Context): Typeface? {
        var typeface = fontCache[fontname]
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.assets, fontname)
            } catch (e: Exception) {
                return null
            }

            fontCache[fontname] = typeface
        }
        return typeface
    }

}
