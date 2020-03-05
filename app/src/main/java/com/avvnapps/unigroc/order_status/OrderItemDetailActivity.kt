package com.avvnapps.unigroc.order_status

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.utils.DateTimeUtils
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_order_details.*
import kotlinx.android.synthetic.main.item_add_rating.*

class OrderItemDetailActivity : AppCompatActivity() {

    lateinit var firestoreViewModel: FirestoreViewModel
    var orderItems: List<CartEntity> = emptyList()
    lateinit var adapter: ItemDetailAdapter
    var orderItem: OrderItem? = null;
    private var mLayoutManager: StaggeredGridLayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        setToolBar()
        orderItem = intent.getParcelableExtra("order") as OrderItem

        orderItems = ArrayList<CartEntity>()
        mLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        rv_order_detail.layoutManager = mLayoutManager

        adapter = ItemDetailAdapter(this, orderItem!!.cartItems)
        rv_order_detail.adapter = adapter
        setViews(orderItem!!)
    }

    private fun setViews(orderItem: OrderItem) {
        tv_order_Detail_total.text = orderItem.quotations[0].quotedPrice.toString()
        tv_order_Detail_retailer_name.text = "Retailer : " + orderItem.quotations[0].retailerName

        var preferredDeliveryTime = "Delivery, " + DateTimeUtils.getPreferredDeliveryDate(

            orderItem.preferredDate,
            orderItem.preferredTimeSlot
        )

        tv_order_delivery_time.text = preferredDeliveryTime

        tv_order_Detail_total_item.text = "Total Items : " + orderItem.cartItems.size.toString()

        var orderStatus = orderItem.orderStatus
        setupOrderStatus(orderStatus, orderItem.isPickup)

        btn_order_detail_cancle.setOnClickListener {
            showReviewDialog(orderStatus)

        }


    }

    private fun showReviewDialog(orderStatus: Int) {

        var dialog = Dialog(this)
        dialog.setContentView(R.layout.item_add_rating)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        val toolbar = findViewById(R.id.orderToolBar) as Toolbar
        toolbar.setTitle("Order Details")
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
