package com.avvnapps.unigroc.database.cart

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Created by Deepak Prasad on 14-02-2019.
 */

@Database(entities = arrayOf(CartEntity::class),version = 1,exportSchema = false)
abstract class CartDatabase : RoomDatabase(){

    abstract fun cartDao() : CartDAO

    companion object {
        // the only instance

        @Volatile
        private var INSTANCE : CartDatabase? = null


        fun getInstance(context: Context): CartDatabase {
            if(INSTANCE == null){
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            CartDatabase::class.java, "cart_database")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                }

            }

            return INSTANCE!!
        }
    }
}