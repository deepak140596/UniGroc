package com.avvnapps.unigroc.models

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * Created by Deepak Prasad on 14-02-2019.
 */

@Entity(tableName = "cart_table")
@Parcelize
data class CartEntity(

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    var itemId: String ="",

    @ColumnInfo(name = "name")
    var name: String ?= null,

    @ColumnInfo(name = "category")
    var category: String ?= null,

    @ColumnInfo(name = "clubbed_category")
    var clubbedCategory: String ?= null,

    @ColumnInfo(name = "photo_url")
    var photoUrl: String ?= null,

    @ColumnInfo(name = "quantity")
    var quantity: Int ?= null,

    @ColumnInfo(name = "price")
    var price: Double ?= null,

    @ColumnInfo(name = "metric_weight")
    var metricWeight: String ?= null

) : SearchSuggestion, Parcelable {

    override fun getBody(): String {
        return "$name in $category"
    }

    constructor() : this("", "", "", "", "", 0, 0.0, "")

    fun incrementQuantity() {
        this.quantity = this.quantity?.plus(1)
    }

    fun decrementQuantity() {
        if (this.quantity == 0) {
            return
        }
        this.quantity = this.quantity?.minus(1)
    }

    override fun toString(): String {
        return "CartEntity(itemId='$itemId', name='$name', category='$category', clubbedCategory='$clubbedCategory', photoUrl='$photoUrl', quantity=$quantity, price=$price, metricWeight='$metricWeight')"
    }


}