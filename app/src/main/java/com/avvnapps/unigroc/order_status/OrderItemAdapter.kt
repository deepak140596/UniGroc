package com.avvnapps.unigroc.order_status

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.models.RetailerQuotationItem
import com.avvnapps.unigroc.utils.DateTimeUtils
import com.avvnapps.unigroc.utils.PriceFormatter
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_order.view.*
import java.util.*


class OrderItemAdapter(
    var context: Context, var orderList: List<OrderItem>,
    var firestoreViewModel: FirestoreViewModel
) : RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {
    var TAG = "CART_ITEM_ADAPTER"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderItem = orderList[position]
        holder.bindItems(context, orderItem, firestoreViewModel)

        //if(orderItem.orderStatus == ApplicationConstants.ORDER_QUOTED)
        if (orderItem.quotations.isNotEmpty() && orderItem.orderStatus < 3) {
            holder.itemView.item_order_view_quotes_action_tv.visibility = View.VISIBLE
        } else {
            holder.itemView.item_order_view_quotes_action_tv.visibility = View.GONE
        }


        holder.itemView.item_order_view_quotes_action_tv.setOnClickListener {
            var expanded = orderItem.isExpanded
            orderItem.isExpanded = !expanded
            notifyItemChanged(position)
        }

        holder.itemView.item_order_view_order_action_tv.setOnClickListener {
            val intent = Intent(context, OrderItemDetailActivity::class.java)
            intent.putExtra("order", orderItem)
            context.startActivity(intent)

        }


        if (orderItem.isPickup)
            holder.itemView.item_order_cancel_action_ll.visibility = View.VISIBLE
        else
            holder.itemView.item_order_cancel_action_ll.visibility = View.GONE

        if (orderItem.quotations.isEmpty() || orderItem.orderStatus < 3)
            holder.itemView.item_order_cancel_action_ll.visibility = View.VISIBLE
        else
            holder.itemView.item_order_cancel_action_ll.visibility = View.GONE


    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var TAG = "CART_ITEM_ADAPTER"
        lateinit var context: Context

        fun bindItems(
            context: Context,
            orderItem: OrderItem,
            firestoreViewModel: FirestoreViewModel
        ) {
            this.context = context

            var preferredDeliveryTime = DateTimeUtils.getPreferredDeliveryDate(
                orderItem.preferredDate,
                orderItem.preferredTimeSlot
            )

            itemView.item_order_estimated_delivery_tv.text = preferredDeliveryTime
            itemView.item_order_id_tv.text = orderItem.orderId.toString()
            itemView.item_order_item_count_tv.text = orderItem.cartItems.size.toString()
            itemView.item_order_status_tv.text = orderItem.orderStatus.toString()
            //itemView.item_order_time_tv.text = orderItem.preferredDate.toString()
            itemView.item_order_quotations_ll.visibility =
                if (orderItem.isExpanded) View.VISIBLE else View.GONE

            itemView.item_order_cancel_action_tv.setOnClickListener {
                showDialogBox(context, orderItem, firestoreViewModel)
            }

            var orderStatus = orderItem.orderStatus
            if (orderItem.quotations.isEmpty())
                orderStatus = 0


            setupStepView(orderStatus)
            setupQuotationView(orderItem, firestoreViewModel)
            setupOrderStatus(orderStatus, getTime(orderStatus, orderItem), orderItem.isPickup)

        }

        private fun setupStepView(step: Int) {
            itemView.item_order_status_stepview.go(step, true)
        }

        private fun setupOrderStatus(orderStatus: Int, time: Long, isPickup: Boolean) {
            var formattedDate = DateTimeUtils.dateTimeFormatter.format(time)
            if (time == 0L)
                formattedDate = ""
            var statusArray = context.resources.getStringArray(R.array.order_status_labels)
            var status = ""

            when (orderStatus) {
                0 -> status = statusArray[0]
                1 -> {
                    status = statusArray[1]
                    formattedDate = ""
                }
                2 -> status = statusArray[2]
                3 -> {
                    status = statusArray[3]
                    formattedDate = ""
                    itemView.item_order_retail1_place_btn.visibility = View.GONE
                    itemView.item_order_retail2_place_btn.visibility = View.GONE
                    itemView.item_order_retail3_place_btn.visibility = View.GONE


                }
                4 -> {
                    itemView.item_order_retail1_place_btn.visibility = View.GONE
                    itemView.item_order_retail2_place_btn.visibility = View.GONE
                    itemView.item_order_retail3_place_btn.visibility = View.GONE

                    if (isPickup)
                        status = statusArray[4]
                    else
                        status = statusArray[5]

                }
                5 -> {
                    itemView.item_order_retail1_place_btn.visibility = View.GONE
                    itemView.item_order_retail2_place_btn.visibility = View.GONE
                    itemView.item_order_retail3_place_btn.visibility = View.GONE
                    itemView.item_order_status_stepview.visibility = View.GONE
                    //***********************
                    itemView.item_order_cancel_action_ll.visibility = View.GONE
                    itemView.item_order_view_quotes_action_tv.visibility = View.GONE


                    if (isPickup)
                        status = statusArray[6]
                    else
                        status = statusArray[7]
                }

            }
            itemView.item_order_status_tv.text = status
            itemView.item_order_time_tv.text = formattedDate
        }

        private fun getTime(orderStatus: Int, orderItem: OrderItem): Long {
            when (orderStatus) {
                0 -> return orderItem.dateSubmitted
                1 -> return orderItem.dateQuoted
                2 -> return orderItem.datePlaced
                3 -> return 0
                4 -> return orderItem.dateReady
                5 -> return orderItem.dateCompleted
            }
            return 0
        }

        private fun showDialogBox(
            context: Context,
            orderItem: OrderItem,
            firestoreViewModel: FirestoreViewModel
        ) {
            var alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle("Cancel Order?")
                .setMessage("Do you want to cancel the order?")
                .setPositiveButton("Yes") { dialog, which ->

                    firestoreViewModel.cancelOrder(orderItem)

                }.setNegativeButton("Cancel") { _, _ ->

                }

            var alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        private fun setupQuotationView(
            orderItem: OrderItem,
            firestoreViewModel: FirestoreViewModel
        ) {
            var quotations = orderItem.quotations
            Collections.sort(quotations, RetailerQuotationItem.compareByRating)
            Log.i(TAG, "Quotations: ${quotations.size}")

            if (quotations.size > 0) {
                itemView.item_order_retail1_name_tv.text = quotations[0].retailerName
                itemView.item_order_retailer1_rating_tv.text = quotations[0].rating.toString()
                itemView.item_order_retail1_quote_price_tv.text =
                    PriceFormatter.getFormattedPrice(context, quotations[0].quotedPrice)

                if (quotations[0].photoUrl != null) {
                    Glide.with(context).load(quotations[0].photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate().into(itemView.item_order_retail1_img)
                }


                /*itemView.item_order_retailer1_distance_tv.text = LocationUtils.getDistance(
                    context as AppCompatActivity,
                    quotations[0].addressItem
                ).toString()*/

                itemView.item_order_retail1_place_btn.setOnClickListener {

                    firestoreViewModel.placeOrder(
                        orderItem,
                        quotations[0].retailerId,
                        quotations[0].cartItems
                    )

                }
            } else {
                itemView.item_order_retail1_details_ll.visibility = View.GONE
                itemView.item_order_retail2_details_ll.visibility = View.GONE
                itemView.item_order_retail3_details_ll.visibility = View.GONE
            }

            if (quotations.size > 1) {
                itemView.item_order_retail2_name_tv.text = quotations[1].retailerName
                itemView.item_order_retailer2_rating_tv.text = quotations[1].rating.toString()
                itemView.item_order_retail2_quote_price_tv.text =
                    PriceFormatter.getFormattedPrice(context, quotations[1].quotedPrice)

                /*itemView.item_order_retailer2_distance_tv.text = LocationUtils.getDistance(
                    context as AppCompatActivity,
                    quotations[1].addressItem
                ).toString()*/

                if (quotations[1].photoUrl != null) {
                    Glide.with(context).load(quotations[1].photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate().into(itemView.item_order_retail2_iv)
                }

                itemView.item_order_retail2_place_btn.setOnClickListener {

                    firestoreViewModel.placeOrder(
                        orderItem,
                        quotations[1].retailerId,
                        quotations[1].cartItems
                    )
                }
            } else {
                itemView.item_order_retail2_details_ll.visibility = View.GONE
                itemView.item_order_retail3_details_ll.visibility = View.GONE
            }

            if (quotations.size > 2) {
                itemView.item_order_retail3_name_tv.text = quotations[2].retailerName
                itemView.item_order_retailer3_rating_tv.text = quotations[2].rating.toString()
                itemView.item_order_retail3_quote_price_tv.text =
                    PriceFormatter.getFormattedPrice(context, quotations[2].quotedPrice)

                /*itemView.item_order_retailer3_distance_tv.text = LocationUtils.getDistance(
                    context as AppCompatActivity,
                    quotations[2].addressItem
                ).toString()*/

                if (quotations[2].photoUrl != null) {
                    Glide.with(context).load(quotations[2].photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate().into(itemView.item_order_retail3_iv)
                }

                itemView.item_order_retail3_place_btn.setOnClickListener {

                    firestoreViewModel.placeOrder(
                        orderItem,
                        quotations[2].retailerId,
                        quotations[2].cartItems
                    )
                }
            } else {
                itemView.item_order_retail3_details_ll.visibility = View.GONE
            }
        }

    }

}