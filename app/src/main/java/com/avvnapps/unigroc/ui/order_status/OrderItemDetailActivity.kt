package com.avvnapps.unigroc.ui.order_status

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.utils.DateTimeUtils
import com.avvnapps.unigroc.utils.PriceFormatter
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_order_details.*
import kotlinx.android.synthetic.main.item_add_rating.*

class OrderItemDetailActivity : AppCompatActivity() {

    lateinit var firestoreViewModel: FirestoreViewModel
    var orderItems: List<CartEntity> = emptyList()
    lateinit var adapter: ItemDetailAdapter
    var orderItem: OrderItem? = null
    private var mLayoutManager: StaggeredGridLayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        setToolBar()
        orderItem = intent.getParcelableExtra("order") as OrderItem

        orderItems = ArrayList<CartEntity>()
        mLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        rv_order_detail.layoutManager = mLayoutManager

        adapter = if (orderItem!!.quotations.isNotEmpty())
            ItemDetailAdapter(this, orderItem!!.quotations[0].cartItems)
        else
            ItemDetailAdapter(this, orderItem!!.cartItems)



        rv_order_detail.adapter = adapter
        setViews(orderItem!!)
    }

    private fun setViews(orderItem: OrderItem) {

        if (orderItem.quotations.isNotEmpty()) {
            tv_order_Detail_total.text =
                PriceFormatter.getFormattedPrice(this, orderItem.quotations[0].quotedPrice)
            tv_order_Detail_retailer_name.text =
                "Retailer : " + orderItem.quotations[0].retailerName
            tv_order_Detail_total_item.text =
                "Total Items : " + orderItem.quotations[0].cartItems.size.toString()


        } else {
            tv_order_Detail_total_item.text = "Total Items : " + orderItem.cartItems.size.toString()

            tv_order_Detail_total.text =
                PriceFormatter.getFormattedPrice(this, setupSubtotal(orderItem))
            tv_order_Detail_retailer_name.text = "Retailer : " + "Not Placed"
        }


        var preferredDeliveryTime = "Delivery, " + DateTimeUtils.getPreferredDeliveryDate(

            orderItem.preferredDate,
            orderItem.preferredTimeSlot
        )

        tv_order_delivery_time.text = preferredDeliveryTime


        var orderStatus = orderItem.orderStatus
        setupOrderStatus(orderStatus, orderItem.isPickup)

        btn_order_detail_cancel.setOnClickListener {
            showReviewDialog(orderStatus)

        }


    }

    private fun setupSubtotal(orderItem: OrderItem): Double {
        var subtotal = 0.0
        for (cartItem in orderItem.cartItems) {
            subtotal += cartItem.price!! * cartItem.quantity!!
        }
        return subtotal
    }


    private fun showReviewDialog(orderStatus: Int) {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.item_add_rating)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.addBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupOrderStatus(orderStatus: Int, isPickup: Boolean) {
        var statusArray = resources.getStringArray(R.array.order_status_labels)
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
                if (isPickup)
                    status = statusArray[6]
                else
                    status = statusArray[7]
            }
        }

        order_item_status_iv.text = status
    }

    private fun setToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.orderToolBar)
        toolbar.title = "Order Details"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
