package com.avvnapps.unigroc.models

import java.io.Serializable

data class wishlistItems(

    var itemId: String = "",
    var no_of_items: Int = 0,
    var user_email: String? = "",
    var user_mobile: String? = "",
    var name: String? = "",
    var price: Double? = 0.0,
    var photoUrl: String? = "",
    var category: String? = "",
    var clubbedCategory: String? = "",
    var metricWeight: String? = ""

) : Serializable {

    constructor() : this("", 0, "", "", "", 0.0, "", "", "", "")

    override fun toString(): String {
        return "wishlistItems(itemId=$itemId, no_of_items=$no_of_items, user_email=$user_email, user_mobile=$user_mobile, name=$name, price=$price, photoUrl=$photoUrl, category=$category, clubbedCategory=$clubbedCategory, metricWeight=$metricWeight)"
    }
}
