package com.avvnapps.unigroc.location_address

import java.io.Serializable

class AddressItem(var addressId:String,var addressName:String,var houseName:String,var locality:String,var landmark:String,
                  var latitude:Double,var longitude:Double) : Serializable{

    constructor():this("","","","","",0.0,0.0)


    fun getAddress():String{
        var address = "$houseName, $locality. Landmark: $landmark"
        return address
    }
}