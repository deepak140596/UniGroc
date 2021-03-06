package com.avvnapps.unigroc.database

import android.content.Context
import androidx.preference.PreferenceManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.AddressItem
import com.avvnapps.unigroc.models.GeoIp
import com.google.gson.Gson

class SharedPreferencesDB {

    companion object {
        fun savePreferredAddress(context: Context, address: AddressItem) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()

            val json = Gson().toJson(address)
            editor.putString(context.getString(R.string.preferred_address), json)

            editor.apply()
            editor.commit()
        }

        fun getSavedAddress(context: Context): AddressItem? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val json =
                sharedPreferences.getString(context.getString(R.string.preferred_address), "")
            if (json!!.isEmpty())
                return null
            return Gson().fromJson(json, AddressItem::class.java)
        }

        fun savePreferredGeoIp(context: Context, geoIp: GeoIp) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()

            val json = Gson().toJson(geoIp)
            editor.putString(context.getString(R.string.preferred_geoip), json)

            editor.apply()
            editor.commit()
        }

        fun getSavedGeoIp(context: Context): GeoIp? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val json = sharedPreferences.getString(context.getString(R.string.preferred_geoip), "")
            if (json!!.isEmpty())
                return null
            return Gson().fromJson(json, GeoIp::class.java)
        }

        fun saveLocationRange(context: Context, double: Double) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()
            editor.putInt("locationRange", double.toInt())
            editor.apply()
            editor.commit()
        }

        fun getLocationRange(context: Context): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val json = sharedPreferences.getInt("locationRange", -1)

            return json
        }
    }

}