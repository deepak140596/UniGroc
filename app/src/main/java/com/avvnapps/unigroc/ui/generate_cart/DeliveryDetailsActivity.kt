package com.avvnapps.unigroc.ui.generate_cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.database.network.ICloudFunctions
import com.avvnapps.unigroc.database.network.RetrofitCloudClient
import com.avvnapps.unigroc.models.AddressItem
import com.avvnapps.unigroc.models.BraintreeTransaction
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.payment.UpiPayment
import com.avvnapps.unigroc.payment.model.PaymentDetail
import com.avvnapps.unigroc.payment.model.TransactionDetails
import com.avvnapps.unigroc.ui.location_address.SavedAddressesActivity
import com.avvnapps.unigroc.utils.DateTimeUtils
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_delivery_details.*
import java.util.*

class DeliveryDetailsActivity : AppCompatActivity() {

    val SET_ADDRESS_REQUEST_CODE = 100
    val TAG = "DELIVERY_DETAILS"

    val TODAY = 0
    val TOMORROW = 1
    var DAY_IN_MIL = 1000 * 60 * 60 * 24

    var deliveryDay = TODAY
    var timeSlot = 0
    var isPickup = true
    var address: AddressItem? = null

    lateinit var cartViewModel: CartViewModel
    lateinit var firestoreViewModel: FirestoreViewModel
    var savedCartItems: List<CartEntity> = emptyList()

    private val REQUEST_CODE: Int = 1234
    internal var token: String? = null
    internal var compositeDisposable = CompositeDisposable()
    internal lateinit var myAPI: ICloudFunctions


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_details)

        // Init
        myAPI = RetrofitCloudClient.instance.create(ICloudFunctions::class.java)

        // Load the Token
        compositeDisposable.add(
            myAPI.getToken()!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ braintreeToken ->
                    token = braintreeToken!!.token
                    Log.d(TAG, token)

                },
                    { throwable ->
                        Toast.makeText(this, "" + throwable.message, Toast.LENGTH_SHORT).show()
                    })
        )




        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
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

    private fun submitPayment() {
        val dropInRequest = DropInRequest().clientToken(token)
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE)
    }

    private fun setupSubtotal(): Double {
        var subtotal = 0.0
        for (cartItem in savedCartItems) {
            subtotal += cartItem.price!! * cartItem.quantity!!
        }

        return subtotal
    }

    fun setOnClickListeners() {
        delivery_details_change_address_btn.setOnClickListener {
            var intent = Intent(this@DeliveryDetailsActivity, SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action", true)
            startActivityForResult(intent, SET_ADDRESS_REQUEST_CODE)
        }

        delivery_details_continue_ll.setOnClickListener {
            //submitOrder()
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
                    continueBtn.text = "Pay Now"
                }
                R.id.delivery_details_pickup_rb -> {
                    isPickup = true
                    continueBtn.text = "Continue"

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

        var timeInHr = DateTimeUtils.get24hrTime()
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
        if (timeInHr < 11) {
            delivery_details_7_11_rb.isChecked = true
            timeSlot = 0
        } else if (timeInHr in 11..12) {
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.isChecked = true
            timeSlot = 1
        } else if (timeInHr in 13..16) {
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.visibility = View.GONE
            delivery_details_1_5_rb.isChecked = true
            timeSlot = 2
        } else if (timeInHr in 17..18) {
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.visibility = View.GONE
            delivery_details_1_5_rb.visibility = View.GONE
            delivery_details_5_7_rb.isChecked = true
            timeSlot = 3
        } else if (timeInHr in 19..20) {
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.visibility = View.GONE
            delivery_details_1_5_rb.visibility = View.GONE
            delivery_details_5_7_rb.visibility = View.GONE
            delivery_details_7_9_rb.isChecked = true
            timeSlot = 4
        } else {
            delivery_details_time_radiogroup.visibility = View.GONE
            delivery_details_no_slots_available_hint_tv.visibility = View.VISIBLE
            timeSlot = -1
        }

        Log.i(TAG, "TIME SLOT: $timeSlot")
    }

    private fun submitOrder() {
        var order = OrderItem()
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
        finish()

    }

    private fun showDialogBox() {
        var alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Place Order?")
            .setMessage("Go ahead and submit order?")
            .setPositiveButton("Yes") { dialog, which ->
                if (isPickup) {
                    submitOrder()
                } else {
                    var geoipVal = SharedPreferencesDB.getSavedGeoIp(this)
                    if (geoipVal!!.currency == "EUR") {
                    }
                    if (geoipVal.currency == "USD") {
                    }
                    if (geoipVal.currency == "GBP") {
                    }
                    if (geoipVal.currency == "INR") {
                        submitPayment()
                        // IndianPayment()
                    }

                }

            }.setNegativeButton("Cancel") { _, _ ->

            }

        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun IndianPayment() {
        // note: always create new instance of PaymentDetail for every new payment/order
        var payment = PaymentDetail(
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result =
                    data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce = result.paymentMethodNonce!!.nonce
                if (!TextUtils.isEmpty(
                        setupSubtotal().toString()
                    )
                ) {
                    compositeDisposable.add(
                        myAPI.submitPayment(
                            setupSubtotal(),
                            nonce
                        )
                            ?.subscribeOn(Schedulers.io())
                            ?.observeOn(AndroidSchedulers.mainThread())
                            ?.subscribe({ t: BraintreeTransaction? ->
                                if (t!!.success) {
                                    submitOrder()
                                    /* Toast.makeText(
                                     this,
                                     "" + t.transaction!!.id,
                                     Toast.LENGTH_SHORT
                                 ).show()*/

                                    Log.d(TAG, t.transaction!!.toString())
                                }
                            },
                                { t: Throwable? ->
                                    Toast.makeText(
                                        this,
                                        "" + t!!.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })!!
                    )
                }

            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()

    }

}
