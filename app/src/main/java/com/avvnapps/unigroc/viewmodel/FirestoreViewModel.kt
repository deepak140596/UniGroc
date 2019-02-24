package com.avvnapps.unigroc.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avvnapps.unigroc.database.cart.CartEntity
import com.avvnapps.unigroc.database.firestore.FirestoreRepository
import com.avvnapps.unigroc.location_address.AddressItem
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class FirestoreViewModel : ViewModel(){

    val TAG = "FIRESTORE_VIEW_MODEL"
    var firebaseRepository = FirestoreRepository()
    var availableCartItems : MutableLiveData<List<CartEntity>> = MutableLiveData()
    var savedAddresses : MutableLiveData<List<AddressItem>> = MutableLiveData()


    // get available cart items from firestore
    fun getAvailableCartItems() : LiveData<List<CartEntity>>{

        availableCartItems = MutableLiveData()
        firebaseRepository.getAvailableCartItems().addOnSuccessListener {documents ->
            var availableCartList : MutableList<CartEntity> = mutableListOf()
            for(doc in documents){
                var cartItem = doc.toObject(CartEntity::class.java)
                availableCartList.add(cartItem)
            }

            availableCartItems.value = availableCartList

        }.addOnFailureListener{
            availableCartItems.value = null
        }

        return availableCartItems

    }

    // save address to firebase
    fun saveAddressToFirebase(addressItem: AddressItem){
        firebaseRepository.saveAddressItem(addressItem).addOnFailureListener {
            Log.e(TAG,"Failed to save Address!")
        }
    }

    // get saved addresses
    /*fun getSavedAddresses(): LiveData<List<AddressItem>>{
        firebaseRepository.getSavedAddress().addOnSuccessListener { documents ->
            var savedAddressList : MutableList<AddressItem> = mutableListOf()
            for(doc in documents){
                var addressItem = doc.toObject(AddressItem::class.java)
                savedAddressList.add(addressItem)
            }
            savedAddresses.value = savedAddressList
        }.addOnFailureListener {
            savedAddresses.value = null
        }

        return savedAddresses
    }*/

    fun getSavedAddresses(): LiveData<List<AddressItem>>{
        firebaseRepository.getSavedAddress().addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                savedAddresses.value = null
                return@EventListener
            }

            var savedAddressList : MutableList<AddressItem> = mutableListOf()
            for (doc in value!!) {
                var addressItem = doc.toObject(AddressItem::class.java)
                savedAddressList.add(addressItem)
            }
            savedAddresses.value = savedAddressList
        })

        return savedAddresses
    }


}