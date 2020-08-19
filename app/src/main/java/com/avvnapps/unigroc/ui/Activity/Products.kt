package com.avvnapps.unigroc.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.ui.generate_cart.CartItemAdapter
import com.avvnapps.unigroc.utils.animateVisibility
import com.avvnapps.unigroc.utils.show
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_products.*


class Products : AppCompatActivity() {
    var TAG = "PRODUCTS_ACTIVITY"

    private val cartViewModel by lazy {
        ViewModelProvider(this).get(CartViewModel::class.java)
    }

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }

    var savedCartItems: List<CartEntity> = emptyList()
    var cartItemAdapter: CartItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        val toolbar = findViewById<Toolbar>(R.id.products_toolbar)
        toolbar.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        // initialise cart view model
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
        })

        // initialise firebase view model
        initialiseFirebaseViewModel()
        cartItemAdapter = CartItemAdapter(this, savedCartItems, cartViewModel)
        products_recycler_view.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = cartItemAdapter
        }.run {
            shimmerProducts.animateVisibility(View.GONE)
            products_recycler_view.show()
        }

        cartItemAdapter?.setOnItemClickListener(object : CartItemAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                val cartItem: CartEntity = cartItemAdapter?.getItem(pos) as CartEntity
                val intent = Intent(this@Products, IndividualProduct::class.java)
                intent.putExtra("product", cartItem)
                startActivity(intent)
            }
        })


    }


    private fun initialiseFirebaseViewModel() {
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            savedCartItems = it
            Log.i(TAG, "Order Size: ${savedCartItems.size}")
            cartItemAdapter?.cartList = savedCartItems
            cartItemAdapter?.notifyDataSetChanged()
        })
    }

    override fun onResume() {
        super.onResume()
        initialiseFirebaseViewModel()
        shimmerProducts.startShimmer()
    }

    override fun onPause() {
        shimmerProducts.stopShimmer()
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}


