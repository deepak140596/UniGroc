package com.avvnapps.unigroc.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.ui.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.utils.PriceFormatter
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_individual_product.*


class IndividualProduct : AppCompatActivity() {
    var firestoreDB = FirebaseFirestore.getInstance()
    var cartItem: CartEntity? = null
    var user = FirebaseAuth.getInstance().currentUser
    var collectionReference =
        firestoreDB.collection("users").document(user!!.email.toString()).collection("wishlist")
    private var flag: Boolean = false
    lateinit var cartViewModel: CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_product)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // initialise cart view model
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartItem = intent.getParcelableExtra("product") as CartEntity
        cartItem!!.quantity = cartViewModel.getQuantity(cartItem!!.itemId)
        if (cartItem!!.quantity == null)
            cartItem!!.quantity = 0
        init()
    }

    private fun init() {

        Glide.with(this).load(cartItem!!.photoUrl)
            .transition(withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate().into(productimage)

        product_name.text = cartItem!!.name.toString()
        product_price.text = PriceFormatter.getFormattedPrice(this, cartItem!!.price!!)


        product_item_cart_add_btn.setOnClickListener {
            cartItem!!.incrementQuantity()
            if (cartItem!!.quantity == 1) {
                cartViewModel.insert(cartItem!!)

            } else {
                cartViewModel.setQuantity(cartItem!!.itemId, cartItem!!.quantity!!)
            }
            updateViews(cartItem!!)

        }
        product_item_cart_add_large_btn.setOnClickListener {

            cartItem!!.incrementQuantity()
            cartViewModel.insert(cartItem!!)


            updateViews(cartItem!!)
        }

        product_item_cart_subtract_btn.setOnClickListener {
            cartItem!!.decrementQuantity()
            if (cartItem!!.quantity == 0) {
                cartViewModel.delete(cartItem!!)


            } else {
                cartViewModel.setQuantity(cartItem!!.itemId, cartItem!!.quantity!!)
            }

            updateViews(cartItem!!)
        }

        add_to_cart.setOnClickListener {
            if (cartItem!!.quantity == 0)
                Toasty.info(
                    this@IndividualProduct,
                    "No Items in Cart",
                    Toast.LENGTH_LONG
                ).show()
            else
                Toasty.success(
                    this@IndividualProduct,
                    "Added in Cart",
                    Toast.LENGTH_LONG
                ).show()
        }

        buy_now_btn.setOnClickListener {
            if (cartItem!!.quantity == 0)
                Toasty.info(
                    this@IndividualProduct,
                    "No Items added",
                    Toast.LENGTH_LONG
                ).show()
            else
                startActivity(Intent(this, ReviewCartActivity::class.java))
        }

    }


    private fun updateViews(cartItem: CartEntity) {
        product_item_cart_quantity_tv.text = cartItem.quantity.toString()
        if (cartItem.quantity == 0) {
            product_item_cart_add_large_btn.visibility = View.VISIBLE
            product_item_cart_subtract_btn.visibility = View.INVISIBLE
        } else {
            product_item_cart_add_large_btn.visibility = View.GONE
            product_item_cart_subtract_btn.visibility = View.VISIBLE
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    fun addToWishlist(view: View) {
        add_to_wishlist.playAnimation()
        val data = hashMapOf(
            "itemId" to cartItem?.itemId.toString(),
            "no_of_items" to Integer.parseInt(cartItem?.quantity.toString()),
            "user_email" to user?.email,
            "user_mobile" to user?.phoneNumber,
            "name" to cartItem?.name,
            "price" to cartItem?.price,
            "photoUrl" to cartItem?.photoUrl,
            "category" to cartItem?.category,
            "clubbedCategory" to cartItem?.clubbedCategory,
            "metricWeight" to cartItem?.metricWeight


        )
        val query = collectionReference.whereEqualTo("itemId", cartItem!!.itemId)
        query.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    flag = true
                }
                if (flag) {

                    Toasty.success(this@IndividualProduct, "Added", Toast.LENGTH_SHORT)
                        .show()

                } else {
                    collectionReference.add(data)
                    Toasty.success(this@IndividualProduct, "Added to Wishlist", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

}
