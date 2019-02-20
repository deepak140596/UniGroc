package com.avvnapps.unigroc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avvnapps.unigroc.database.cart.CartEntity
import com.avvnapps.unigroc.database.firestore.FirestoreRepository
class FirestoreViewModel : ViewModel(){


    var firebaseRepository = FirestoreRepository()
    var availableCartItems : MutableLiveData<List<CartEntity>> = MutableLiveData()


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


}