package com.avvnapps.unigroc.database.cart

import androidx.lifecycle.LiveData
import androidx.room.*
import com.avvnapps.unigroc.models.CartEntity

/**
 * Created by Deepak Prasad on 14-02-2019.
 */

@Dao
interface CartDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cartEntity: CartEntity)

    @Delete
    fun delete(cartEntity: CartEntity)

    @Query("DELETE FROM cart_table")
    fun deleteAll()

    @Query("SELECT * FROM cart_table WHERE quantity <> 0")
    fun getCartList(): LiveData<List<CartEntity>>

    @Query("UPDATE cart_table SET quantity = :qty WHERE item_id = :id")
    fun setQuantity(id: String, qty: Int)

    @Query("SELECT quantity FROM cart_table WHERE item_id = :id")
    fun getQuantity(id: String): Int

}