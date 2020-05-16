package com.avvnapps.unigroc.database.network

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://us-central1-unigroc.cloudfunctions.net/payment/"

object RetrofitCloudClient {
    private var ourInstantce: Retrofit? = null

    val instance: Retrofit
        get() {
            if (ourInstantce == null)
                ourInstantce = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return ourInstantce!!
        }
}