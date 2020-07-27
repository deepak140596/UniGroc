package com.avvnapps.unigroc.ui.order_status

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.ui.Activity.AddReviewActivity
import com.avvnapps.unigroc.utils.*
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_order_details.*

class OrderItemDetailActivity : AppCompatActivity() {

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }
    var orderItems: List<CartEntity> = emptyList()
    private lateinit var itemDetailAdapter: ItemDetailAdapter
    var orderItem: OrderItem? = null
    private val mLayoutManager by lazy {
        StaggeredGridLayoutManager(
            1,
            StaggeredGridLayoutManager.VERTICAL
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        setToolBar()
        orderItem = intent.getParcelableExtra<OrderItem>("order") as OrderItem
        orderItems = ArrayList<CartEntity>()

        itemDetailAdapter = if (orderItem!!.quotations.isNotEmpty()) {
            ll_order_summary_retailer.show()
            ItemDetailAdapter(this, orderItem!!.quotations[0].cartItems)
        } else {
            ll_order_summary_retailer.hide()
            ItemDetailAdapter(this, orderItem!!.cartItems)
        }

        rv_order_detail.apply {
            layoutManager = mLayoutManager
            adapter = itemDetailAdapter
        }

        setViews(orderItem!!)
    }

    @SuppressLint("SetTextI18n")
    private fun setViews(orderItem: OrderItem) {

        if (orderItem.quotations.isNotEmpty()) {
            tv_order_Detail_total.text =
                PriceFormatter.getFormattedPrice(this, orderItem.quotations[0].quotedPrice)
            tv_order_Detail_retailer_name.text =
                "Retailer : ${orderItem.quotations[0].retailerName}"
            tv_order_Detail_total_item.text =
                "Total Items : " + orderItem.quotations[0].cartItems.size.toString()


            tv_order_summary_retailer_name.text = orderItem.quotations[0].retailerName
            val address = LocationUtils.getAddress(
                this,
                orderItem.quotations[0].addressItem.latitude,
                orderItem.quotations[0].addressItem.longitude
            )
            if (address != null) {
                tv_order_summary_retailer_address.text = address
            } else {
                tv_order_summary_retailer_address.text = "Select Address"
            }

            if (orderItem.isPickup) {
                tv_order_summary_retailer_order_done.text =
                    "This order with ${orderItem.quotations[0].retailerName} was picked"
            } else {
                tv_order_summary_retailer_order_done.text =
                    "This order with ${orderItem.quotations[0].retailerName} was delivered"
            }


        } else {
            tv_order_Detail_total_item.text = "Total Items : " + orderItem.cartItems.size.toString()

            tv_order_Detail_total.text =
                PriceFormatter.getFormattedPrice(this, setupSubtotal(orderItem))
            tv_order_Detail_retailer_name.text = "Retailer : " + "Not Placed"
        }


        val preferredDeliveryTime = "Delivery, " + DateTimeUtils.getPreferredDeliveryDate(

            orderItem.preferredDate,
            orderItem.preferredTimeSlot
        )

        tv_order_delivery_time.text = preferredDeliveryTime


        val orderStatus = orderItem.orderStatus
        setupOrderStatus(orderStatus, orderItem.isPickup)

        btn_order_detail_review.setOnClickListener {
            startActivity(Intent(this, AddReviewActivity::class.java))
        }


    }

    private fun setupSubtotal(orderItem: OrderItem): Double {
        var subtotal = 0.0
        for (cartItem in orderItem.cartItems) {
            subtotal += cartItem.price!! * cartItem.quantity!!
        }
        return subtotal
    }


    private fun setupOrderStatus(orderStatus: Int, isPickup: Boolean) {
        val statusArray = resources.getStringArray(R.array.order_status_labels)
        var status = ""

        when (orderStatus) {
            0 -> status = statusArray[0]
            1 -> {
                status = statusArray[1]
            }
            2 -> status = statusArray[2]
            3 -> {
                status = statusArray[3]
            }
            4 -> {
                if (isPickup)
                    status = statusArray[4]
                else
                    status = statusArray[5]
            }
            5 -> {
                if (isPickup) {
                    btn_order_detail_review.show()
                    status = statusArray[6]
                } else {
                    status = statusArray[7]
                }

            }
        }

        order_item_status_iv.text = status
    }

    private fun setToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.orderToolBar)
        toolbar.title = "Order Summary"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
