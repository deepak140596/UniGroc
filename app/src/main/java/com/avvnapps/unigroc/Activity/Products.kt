package com.avvnapps.unigroc.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.generate_cart.CartItemAdapter
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import kotlinx.android.synthetic.main.activity_products.*


class Products : AppCompatActivity() {
    var TAG = "PRODUCTS_ACTIVITY"

    lateinit var cartViewModel: CartViewModel
    lateinit var firestoreViewModel: FirestoreViewModel
    var savedCartItems: List<CartEntity> = emptyList()
    lateinit var adapter: CartItemAdapter
    private var mLayoutManager: StaggeredGridLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        // initialise cart view model
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it

        })

        // initialise firebase view model
        initialiseFirebaseViewModel()
        //using staggered grid pattern in recyclerview
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        products_recycler_view.layoutManager = mLayoutManager
        adapter = CartItemAdapter(this, savedCartItems, cartViewModel)
        products_recycler_view.adapter = adapter

        adapter.setOnItemClickListener(object : CartItemAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                val cartItem: CartEntity = adapter.getItem(pos) as CartEntity;
                if (cartItem == null)
                    return;
                val intent = Intent(this@Products, IndividualProduct::class.java)
                intent.putExtra("product", cartItem)
                startActivity(intent)

            }
        })


    }


    private fun initialiseFirebaseViewModel() {

        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            savedCartItems = it
            Log.i(TAG, "Order Size: ${savedCartItems.size}")
            adapter.cartList = savedCartItems
            adapter.notifyDataSetChanged()
        })
    }

    override fun onResume() {
        super.onResume()
        initialiseFirebaseViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}


