package com.avvnapps.unigroc.database.firestore

import com.avvnapps.unigroc.models.AddressItem
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.utils.ApplicationConstants
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class FirestoreRepository {

    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var email = FirebaseAuth.getInstance().currentUser!!.email.toString()


    // get available cart items
    fun getAvailableCartItems(): Task<QuerySnapshot> {
        val collectionReference = firestoreDB.collection("available_cart_items").limit(10)
        return collectionReference.get()
    }

    //get Wishlist Items
    fun getWishlistItems(): Task<QuerySnapshot> {
        val collectionReference = firestoreDB.collection("users").document(email)
            .collection("wishlist")
        return collectionReference.get()
    }

    // save address to firebase
    fun saveAddressItem(addressItem: AddressItem): Task<Void> {
        //var
        val documentReference = firestoreDB.collection("users").document(email)
            .collection("saved_addresses").document(addressItem.addressId)
        return documentReference.set(addressItem)
    }

    // get saved addresses from firebase
    fun getSavedAddress(): CollectionReference {
        return firestoreDB.collection("users/$email/saved_addresses")
    }

    fun deleteAddress(addressItem: AddressItem): Task<Void> {
        val documentReference = firestoreDB.collection("users/$email/saved_addresses")
            .document(addressItem.addressId)

        return documentReference.delete()
    }

    fun deleteOrderItem(orderItem: OrderItem): Task<Void> {
        val documentReference = firestoreDB.collection("orders")
            .document(orderItem.orderId.toString())

        return documentReference.delete()
    }

    fun submitOrder(orderItem: OrderItem): Task<Void> {
        val documentReference =
            firestoreDB.collection("orders").document(orderItem.orderId.toString())
        return documentReference.set(orderItem)
    }

    fun getQuotedPrices(orderItem: OrderItem): CollectionReference {
        return firestoreDB.collection("orders").document(orderItem.orderId.toString())
            .collection("quotations")
    }

    fun getQuotedOrders(): Query {
        return firestoreDB.collection("orders")
            .whereLessThan("orderStatus", ApplicationConstants.ORDER_PICKED_DELIVERED)
            .whereEqualTo("customerId", email)
    }

    fun getOrderHistory(): Task<QuerySnapshot> {
        val collectionReference = firestoreDB.collection("orders")
            .whereEqualTo("customerId", email)
            .whereEqualTo("orderStatus", ApplicationConstants.ORDER_PICKED_DELIVERED)

        return collectionReference.get()
    }

    fun getAllOrders(): Query {
        return firestoreDB.collection("orders")
            .whereEqualTo("customerId", email)
    }

    fun placeOrder(
        orderItem: OrderItem,
        retailerId: String,
        cartItems: List<CartEntity>
    ): Task<Void> {
        var documentReference =
            firestoreDB.collection("orders").document(orderItem.orderId.toString())
        return documentReference.update(
            "retailerId", retailerId,
            "cartItems", cartItems,
            "orderStatus", ApplicationConstants.ORDER_PREPARING,
            "datePlaced", Date().time
        )
    }

}