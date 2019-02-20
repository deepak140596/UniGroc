package com.avvnapps.unigroc.database.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FirestoreRepository {

    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()


    fun getAvailableCartItems(): Task<QuerySnapshot> {
        var collectionReference = firestoreDB.collection("available_cart_items")
        return collectionReference.get()
    }
}