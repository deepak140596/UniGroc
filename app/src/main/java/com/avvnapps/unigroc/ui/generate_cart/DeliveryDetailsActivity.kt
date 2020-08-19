package com.avvnapps.unigroc.ui.generate_cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.models.AddressItem
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.payment.UpiPayment
import com.avvnapps.unigroc.payment.model.PaymentDetail
import com.avvnapps.unigroc.payment.model.TransactionDetails
import com.avvnapps.unigroc.ui.MainActivity
import com.avvnapps.unigroc.ui.location_address.SavedAddressesActivity
import com.avvnapps.unigroc.utils.DateTimeUtils
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_delivery_details.*
import java.util.*

class DeliveryDetailsActivity : AppCompatActivity() {

    private val SET_ADDRESS_REQUEST_CODE = 100
    val TAG = "DELIVERY_DETAILS"

    private val TODAY = 0
    private val TOMORROW = 1
    private var DAY_IN_MIL = 1000 * 60 * 60 * 24

    private var deliveryDay = TODAY
    var timeSlot = 0
    var isPickup = true
    var address: AddressItem? = null


    private var savedCartItems: List<CartEntity> = emptyList()


    private val firestoreViewModel by lazy { ViewModelProvider(this).get(FirestoreViewModel::class.java) }
    private val cartViewModel by lazy { ViewModelProvider(this).get(CartViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_details)

        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            Log.i(TAG, "Saved Cart items size: ${savedCartItems.size}")
        })

        setOnClickListeners()
        setOnCheckedChangedListeners()
        setUpTimeSlots()

    }

    override fun onResume() {
        super.onResume()
        setUpDeliveryLocation()
    }

    private fun setupSubtotal(): Double {
        var subtotal = 0.0
        for (cartItem in savedCartItems) {
            subtotal += cartItem.price!! * cartItem.quantity!!
        }

        return subtotal
    }

    private fun setOnClickListeners() {
        delivery_details_change_address_btn.setOnClickListener {
            val intent = Intent(this@DeliveryDetailsActivity, SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action", true)
            startActivityForResult(intent, SET_ADDRESS_REQUEST_CODE)
        }

        delivery_details_continue_ll.setOnClickListener {
            showDialogBox()
        }


    }

    private fun setOnCheckedChangedListeners() {
        // select day for delivery/pickup
        delivery_details_day_radiogroup.setOnClickedButtonListener { _, position ->
            if (position == 0)
                deliveryDay = TODAY
            else if (position == 1)
                deliveryDay = TOMORROW

            // update the time slots once a day is chosen
            setUpTimeSlots()
        }

        // select time slot for pickup/delivery
        delivery_details_time_radiogroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.delivery_details_7_11_rb -> {
                    timeSlot = 0
                }
                R.id.delivery_details_11_1_rb -> {
                    timeSlot = 1
                }
                R.id.delivery_details_1_5_rb -> {
                    timeSlot = 2
                }
                R.id.delivery_details_5_7_rb -> {
                    timeSlot = 3
                }
                R.id.delivery_details_7_9_rb -> {
                    timeSlot = 4
                }
            }
            Log.i(TAG, "TIME SLOT: $timeSlot")
        }

        // select pickup or delivery
        delivery_details_type_radiogroup.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {
                R.id.delivery_details_delivery_rb -> {
                    isPickup = false
                }
                R.id.delivery_details_pickup_rb -> {
                    isPickup = true

                }
            }
            Log.i(TAG, "isPickup: $isPickup")
        }

    }

    private fun setUpDeliveryLocation() {
        address = SharedPreferencesDB.getSavedAddress(this)
        if (address == null) {
            delivery_details_address_name_tv.text = getString(R.string.hint_select_default_address)
        } else {
            //Toasty.info(this@DeliveryDetailsActivity,"Address: "+addressId).show()

            delivery_details_address_name_tv.text = address!!.addressName
            delivery_details_address_one_tv.text = address!!.houseName
            delivery_details_address_two_tv.text = address!!.locality
            delivery_details_landmark_tv.text = "Landmark: ${address!!.landmark}"
        }
    }

    private fun setUpTimeSlots() {

        val timeInHr = DateTimeUtils.get24hrTime()
        delivery_details_time_radiogroup.visibility = View.VISIBLE
        delivery_details_no_slots_available_hint_tv.visibility = View.GONE

        Log.i(TAG, "DAY: $deliveryDay")
        // if delivery day is for tomorrow
        if (deliveryDay == TOMORROW) {
            delivery_details_7_11_rb.visibility = View.VISIBLE
            delivery_details_11_1_rb.visibility = View.VISIBLE
            delivery_details_1_5_rb.visibility = View.VISIBLE
            delivery_details_5_7_rb.visibility = View.VISIBLE
            delivery_details_7_9_rb.visibility = View.VISIBLE

            delivery_details_7_11_rb.isChecked = true
            timeSlot = 0
            Log.i(TAG, "TIME SLOT: $timeSlot")
            return
        }

        // if delivery day is today
        when {
            timeInHr < 11 -> {
                delivery_details_7_11_rb.isChecked = true
                timeSlot = 0
            }
            timeInHr in 11..12 -> {
                delivery_details_7_11_rb.visibility = View.GONE
                delivery_details_11_1_rb.isChecked = true
                timeSlot = 1
            }
            timeInHr in 13..16 -> {
                delivery_details_7_11_rb.visibility = View.GONE
                delivery_details_11_1_rb.visibility = View.GONE
                delivery_details_1_5_rb.isChecked = true
                timeSlot = 2
            }
            timeInHr in 17..18 -> {
                delivery_details_7_11_rb.visibility = View.GONE
                delivery_details_11_1_rb.visibility = View.GONE
                delivery_details_1_5_rb.visibility = View.GONE
                delivery_details_5_7_rb.isChecked = true
                timeSlot = 3
            }
            timeInHr in 19..20 -> {
                delivery_details_7_11_rb.visibility = View.GONE
                delivery_details_11_1_rb.visibility = View.GONE
                delivery_details_1_5_rb.visibility = View.GONE
                delivery_details_5_7_rb.visibility = View.GONE
                delivery_details_7_9_rb.isChecked = true
                timeSlot = 4
            }
            else -> {
                delivery_details_time_radiogroup.visibility = View.GONE
                delivery_details_no_slots_available_hint_tv.visibility = View.VISIBLE
                timeSlot = -1
            }
        }

        Log.i(TAG, "TIME SLOT: $timeSlot")
    }

    private fun submitOrder() {
        val order = OrderItem()
        order.cartItems = savedCartItems
        order.isPickup = isPickup
        order.preferredDate = Date().time + deliveryDay * DAY_IN_MIL
        order.preferredTimeSlot = timeSlot
        order.address = address!!

        Log.i(TAG, "Order details: $order")

        // submit the order and clear the cart
        firestoreViewModel.submitOrder(order, cartViewModel)
        cartViewModel.deleteAll()
        //addQuotations(order.orderId.toString())
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()

    }

    private fun showDialogBox() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Place Order?")
            .setMessage("Go ahead and submit order?")
            .setPositiveButton("Yes") { dialog, which ->
                submitOrder()
            }.setNegativeButton("No") { _, _ ->

            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun IndianPayment() {
        // note: always create new instance of PaymentDetail for every new payment/order
        val payment = PaymentDetail(
            vpa = "ibby1561-3@okhdfcbank",
            name = "Ahraar Alam",
            payeeMerchantCode = "",       // only if you have merchantCode else pass empty string
            txnRefId = "",                // if you pass empty string we will generate txnRefId for you
            description = "description",
            amount = "1.00"
        )              // format of amount should be in decimal format x.x (eg 530.00), max. 2 decimal places

        // note: always create new instance of UpiPayment for every new payment/order
        UpiPayment(this)
            .setPaymentDetail(payment)
            .setUpiApps(UpiPayment.UPI_APPS)
            .setCallBackListener(object : UpiPayment.OnUpiPaymentListener {
                override fun onSubmitted(data: TransactionDetails) {
                    //transaction pending: use data to get TransactionDetails
                    Log.i("UPI_Details : ", data.toString())
                    submitOrder()


                }

                override fun onSuccess(data: TransactionDetails) {
                    //transaction success: use data to get TransactionDetails
                    Log.i("UPI_Details : ", data.toString())
                    submitOrder()

                }

                override fun onError(message: String) {

                    //user backpress or transaction failed
                    Toasty.info(
                        this@DeliveryDetailsActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }).pay()
    }



}
