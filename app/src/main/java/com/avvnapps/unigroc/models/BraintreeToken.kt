package com.avvnapps.unigroc.models

import com.google.gson.annotations.SerializedName

data class BraintreeToken(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("token")
    val token: String
)