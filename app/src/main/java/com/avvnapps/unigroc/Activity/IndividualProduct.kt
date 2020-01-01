package com.avvnapps.unigroc.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.utils.PriceFormatter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_individual_product.*


class IndividualProduct : AppCompatActivity() {
    private var quantity = 1
    var firestoreDB = FirebaseFirestore.getInstance()
    var cartItem: CartEntity? = null;
    var user = FirebaseAuth.getInstance().currentUser
    var collectionReference = firestoreDB.collection("users").document(user!!.email.toString()).collection("wishlist")
    private var flag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_product)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        cartItem = intent.getParcelableExtra("product") as CartEntity

        init()
    }

    private fun init() {
        quantityProductPage.setText("1")
        //setting textwatcher for no of items field
        quantityProductPage.addTextChangedListener(productcount)
        Glide.with(this).load(cartItem!!.photoUrl)
            .transition(withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate().into(productimage)

        product_name.setText(cartItem!!.name)
        product_price.text = PriceFormatter.getFormattedPrice(cartItem!!.price!!)



    }

    fun decrement(view: View) {
        if (quantity > 1) {
            quantity--
            quantityProductPage.setText(quantity.toString())
        }
    }

    fun increment(view: View) {
        if (quantity < 500) {
            quantity++
            quantityProductPage.setText(quantity.toString())
        } else {
            Toasty.error(
                this@IndividualProduct,
                "Product Count Must be less than 500",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //check that product count must not exceed 500
    internal var productcount: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            //none
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (quantityProductPage.text.toString() == "") {
                quantityProductPage.setText("0")
            }
        }

        override fun afterTextChanged(s: Editable) {
            //none
            if (Integer.parseInt(quantityProductPage.text.toString()) >= 500) {
                Toasty.error(
                    this@IndividualProduct,
                    "Product Count Must be less than 500",
                    Toast.LENGTH_LONG
                ).show()
            }
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
            "no_of_items" to Integer.parseInt(quantityProductPage.text.toString()),
            "user_email" to user?.email,
            "user_mobile" to  user?.phoneNumber,
            "name" to   cartItem?.name,
            "price" to cartItem?.price,
            "photoUrl" to  cartItem?.photoUrl,
            "category" to  cartItem?.category,
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
