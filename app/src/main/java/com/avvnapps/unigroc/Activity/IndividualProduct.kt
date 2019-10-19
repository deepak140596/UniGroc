package com.avvnapps.unigroc.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.utils.PriceFormatter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_individual_product.*

class IndividualProduct : AppCompatActivity() {
    private var quantity = 1

    var cartItem: CartEntity? = null;
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

}
