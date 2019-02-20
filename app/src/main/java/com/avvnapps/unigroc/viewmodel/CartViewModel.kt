package com.avvnapps.unigroc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.avvnapps.unigroc.database.cart.CartDatabase
import com.avvnapps.unigroc.database.cart.CartEntity
import com.avvnapps.unigroc.database.cart.CartRepository
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by Deepak Prasad on 14-02-2019.
 */
class CartViewModel(application: Application): AndroidViewModel(application){
    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    val repository: CartRepository
    val cartList : LiveData<List<CartEntity>>

    init {
        val cartDAO = CartDatabase.getInstance(application).cartDao()
        repository = CartRepository(cartDAO)
        cartList = repository.cartList  // same as repository.getCartList()

    }

    fun insert(cartEntity: CartEntity) = scope.launch(Dispatchers.IO){
        repository.insert(cartEntity)
    }

    fun delete(cartEntity: CartEntity) = scope.launch(Dispatchers.IO){
        repository.delete(cartEntity)
    }

    fun deleteAll() = scope.launch(Dispatchers.IO){
        repository.deleteAll()
    }

    fun setQuantity(id:String, qty:Int) = scope.launch(Dispatchers.IO){
        repository.setQuantity(id,qty)
    }

    fun getQuantity(id:String):Int {
        return repository.getQuantity(id)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}