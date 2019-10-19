package com.avvnapps.unigroc.generate_cart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.utils.PriceFormatter
import com.avvnapps.unigroc.viewmodel.CartViewModel
import kotlinx.android.synthetic.main.activity_review_cart.*
import kotlinx.android.synthetic.main.view_cart_total.*

class ReviewCartActivity : AppCompatActivity() {

    lateinit var cartViewModel: CartViewModel
    lateinit var savedCartItems : List<CartEntity>
    lateinit var adapter: CartItemAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_cart)

        // setup toolbar

        review_cart_toolbar.title = "Review Cart"
        setSupportActionBar(review_cart_toolbar)

        // setup recyclerview
        review_cart_recycler_view.layoutManager = LinearLayoutManager(this)
        review_cart_recycler_view.addItemDecoration(
            DividerItemDecoration(
                review_cart_recycler_view.context,DividerItemDecoration.VERTICAL
            )
        )
        // initialise the viewmodel to pass into adapter
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        savedCartItems = ArrayList<CartEntity>()
        adapter = CartItemAdapter(this,savedCartItems,cartViewModel)
        review_cart_recycler_view.adapter = adapter

        // get saved cart items from local database
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            if (savedCartItems.isEmpty()){
                review_cart_toolbar.title = "Cart"
                empty_layout.visibility = View.VISIBLE
                cart_layout.visibility = View.GONE

            }else{
                cart_layout.visibility = View.VISIBLE
                empty_layout.visibility = View.GONE

            }

            adapter.cartList = savedCartItems
            adapter.notifyDataSetChanged()
            setupSubtotal()
        })


        view_cart_place_order_ll.setOnClickListener {
            startActivity(Intent(this,DeliveryDetailsActivity::class.java))
            finish()
        }

        btn_shopnow.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

    }

    private fun setupSubtotal(){
        var subtotal = 0.0
        for(cartItem in savedCartItems){
            subtotal += cartItem.price!! * cartItem.quantity!!
        }
        view_cart_total_tv.text = PriceFormatter.getFormattedPrice(subtotal)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_review_cart,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId){
            R.id.menu_review_cart_search ->{
                startActivity(Intent(this@ReviewCartActivity,SearchItemActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
