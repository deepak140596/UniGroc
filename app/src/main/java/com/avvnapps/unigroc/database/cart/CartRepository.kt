package com.avvnapps.unigroc.database.cart

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import com.avvnapps.unigroc.models.CartEntity

/**
 * Created by Deepak Prasad on 14-02-2019.
 */


class CartRepository(private val cartDAO: CartDAO) {

    // get cart list
    val cartList: LiveData<List<CartEntity>> = cartDAO.getCartList()

    @WorkerThread
    suspend fun insert(cartEntity: CartEntity) {
        cartDAO.insert(cartEntity)
    }

    @WorkerThread
    suspend fun delete(cartEntity: CartEntity) {
        cartDAO.delete(cartEntity)
    }

    @WorkerThread
    suspend fun deleteAll() {
        cartDAO.deleteAll()
    }

    @WorkerThread
    suspend fun setQuantity(id: String, qty: Int) {
        cartDAO.setQuantity(id, qty)
    }

    fun getQuantity(id: String): Int {
        return cartDAO.getQuantity(id)
    }


}