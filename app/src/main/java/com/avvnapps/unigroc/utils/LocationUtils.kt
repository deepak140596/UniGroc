package com.avvnapps.unigroc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.avvnapps.unigroc.models.AddressItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.type.LatLng
import java.io.IOException
import java.lang.Exception
import java.util.*

class LocationUtils(context: Context){

    private var fusedLocationProviderClient: FusedLocationProviderClient ?= null
    private var location : MutableLiveData<Location> = MutableLiveData()

    // call constructor to get location
    init {
        getInstance(context)
        getLocation()
    }

    // using singleton pattern to get the locationProviderClient
    fun getInstance(appContext: Context): FusedLocationProviderClient{
        if(fusedLocationProviderClient == null)
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)
        return fusedLocationProviderClient!!
    }


    @SuppressLint("MissingPermission")
    fun getLocation() : LiveData<Location> {

        fusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener {loc: Location? ->
                location.value = loc!!
            }
        return location
    }


    companion object {
        fun getAddress(activity: AppCompatActivity,lat: Double, lng: Double): String? {

            //Log.d(TAG, "get Address for LAT: $lat  LON: $lng")
            if (lat == 0.0 && lng == 0.0)
                return null
            val geocoder = Geocoder(activity, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val obj = addresses[0]
                var add = if (obj.thoroughfare == null) "" else obj.thoroughfare + ", "

                add += if (obj.subLocality == null) "" else obj.subLocality + ", "
                add += if (obj.subAdminArea == null) "" else obj.subAdminArea


                //Log.v(TAG, "Address received: $add")

                return add
            } catch (e: IOException) {

                e.printStackTrace()
            }

            return ""
        }

        fun getDistance(activity: AppCompatActivity,addressItem: AddressItem): Float {
            var loc1 = Location("Location1")
            loc1.longitude = addressItem.longitude
            loc1.latitude = addressItem.latitude

            var loc2 = Location("Location2")
            var l = LocationUtils(activity).location.value
            loc2.longitude = l!!.longitude
            loc2.latitude = l!!.latitude
            return loc1.distanceTo(loc2)
        }
    }

}