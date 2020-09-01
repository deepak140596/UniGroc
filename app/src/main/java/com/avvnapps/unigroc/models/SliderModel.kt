package com.avvnapps.unigroc.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class SliderModel(
    var banner: String? = null,
    var backgroundColor: String? = null
) : Parcelable