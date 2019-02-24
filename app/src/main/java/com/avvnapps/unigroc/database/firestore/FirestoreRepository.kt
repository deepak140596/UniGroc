package com.avvnapps.unigroc.database.firestore

import android.util.Log
import com.avvnapps.unigroc.location_address.AddressItem
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class FirestoreRepository {

    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser


    // get availbale cart items
    fun getAvailableCartItems(): Task<QuerySnapshot> {
        var collectionReference = firestoreDB.collection("available_cart_items")
        return collectionReference.get()
    }

    // save address to firebase
    fun saveAddressItem(addressItem: AddressItem): Task<Void> {
        //var
        var documentReference = firestoreDB.collection("users").document(user!!.email.toString())
            .collection("saved_addresses").document(addressItem.addressId.toString())
        return documentReference.set(addressItem)
    }

    // get saved address from firebase
    fun getSavedAddres(): Task<QuerySnapshot> {
        var collectionReference = firestoreDB.collection("users/${user!!.email.toString()}/saved_addresses")
        return collectionReference.get()
    }

    fun getSavedAddress(): CollectionReference {
        var collectionReference = firestoreDB.collection("users/${user!!.email.toString()}/saved_addresses")
        return collectionReference
    }
}