package com.avvnapps.unigroc.generate_cart

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.avvnapps.unigroc.NavigationActivity
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.AddressItem
import com.avvnapps.unigroc.location_address.SavedAddressesActivity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.models.RetailerQuotationItem
import com.avvnapps.unigroc.navigation_fragments.DashboardFragment
import com.avvnapps.unigroc.utils.ApplicationConstants
import com.avvnapps.unigroc.utils.DateTimeUtils
import com.avvnapps.unigroc.utils.SuccessToast
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
    var address : AddressItem?= null

    lateinit var cartViewModel : CartViewModel
    lateinit var firestoreViewModel: FirestoreViewModel
    var savedCartItems : List<CartEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_details)

        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            Log.i(TAG,"Saved Cart items size: ${savedCartItems.size}")
        })


        setOnClickListeners()
        setOnCheckedChangedListeners()
        setUpTimeSlots()

    }

    override fun onResume() {
        super.onResume()
        setUpDeliveryLocation()
    }

    fun setOnClickListeners(){
        delivery_details_change_address_btn.setOnClickListener {
            var intent = Intent(this@DeliveryDetailsActivity,SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action",true)
            startActivityForResult(intent,SET_ADDRESS_REQUEST_CODE)
        }

        delivery_details_continue_ll.setOnClickListener {
            //submitOrder()
            showDialogBox()
        }


    }
    fun setOnCheckedChangedListeners(){
        // select day for delivery/pickup
        delivery_details_day_radiogroup.setOnClickedButtonListener{_ , position ->
            if(position == 0)
                deliveryDay = TODAY
            else if(position == 1)
                deliveryDay = TOMORROW

            // update the time slots once a day is chosen
            setUpTimeSlots()
        }

        // select time slot for pickup/delivery
        delivery_details_time_radiogroup.setOnCheckedChangeListener{_ , checkedId ->
            when(checkedId){
                R.id.delivery_details_7_11_rb ->{
                    timeSlot = 0
                }
                R.id.delivery_details_11_1_rb ->{
                    timeSlot = 1
                }
                R.id.delivery_details_1_5_rb ->{
                    timeSlot = 2
                }
                R.id.delivery_details_5_7_rb ->{
                    timeSlot = 3
                }
                R.id.delivery_details_7_9_rb ->{
                    timeSlot = 4
                }
            }
            Log.i(TAG,"TIME SLOT: $timeSlot")
        }

        // select pickup or delivery
        delivery_details_type_radiogroup.setOnCheckedChangeListener { _, checkedId ->

            when(checkedId){
                R.id.delivery_details_delivery_rb ->{
                    isPickup = false
                }
                R.id.delivery_details_pickup_rb ->{
                    isPickup = true
                }
            }
            Log.i(TAG,"isPickup: $isPickup")
        }

    }

    fun setUpDeliveryLocation(){
        address = SharedPreferencesDB.getSavedAddress(this)
        if(address == null){
            delivery_details_address_name_tv.text = getString(R.string.hint_select_default_address)
        }else{
            //Toasty.info(this@DeliveryDetailsActivity,"Address: "+addressId).show()

            delivery_details_address_name_tv.text = address!!.addressName
            delivery_details_address_one_tv.text = address!!.houseName
            delivery_details_address_two_tv.text = address!!.locality
            delivery_details_landmark_tv.text = "Landmark: ${address!!.landmark}"
        }
    }

    fun setUpTimeSlots(){

        var timeInHr = DateTimeUtils.get24hrTime()
        delivery_details_time_radiogroup.visibility = View.VISIBLE
        delivery_details_no_slots_available_hint_tv.visibility = View.GONE

        Log.i(TAG,"DAY: $deliveryDay")
        // if delivery day is for tomorrow
        if(deliveryDay == TOMORROW){
            delivery_details_7_11_rb.visibility = View.VISIBLE
            delivery_details_11_1_rb.visibility = View.VISIBLE
            delivery_details_1_5_rb.visibility = View.VISIBLE
            delivery_details_5_7_rb.visibility = View.VISIBLE
            delivery_details_7_9_rb.visibility = View.VISIBLE

            delivery_details_7_11_rb.isChecked = true
            timeSlot = 0
            Log.i(TAG,"TIME SLOT: $timeSlot")
            return
        }

        // if delivery day is today
        if(timeInHr<11){
            delivery_details_7_11_rb.isChecked = true
            timeSlot = 0
        }else if(timeInHr in 11..12){
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.isChecked = true
            timeSlot = 1
        }else if(timeInHr in 13..16){
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.visibility = View.GONE
            delivery_details_1_5_rb.isChecked = true
            timeSlot = 2
        }else if(timeInHr in 17..18){
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.visibility = View.GONE
            delivery_details_1_5_rb.visibility = View.GONE
            delivery_details_5_7_rb.isChecked = true
            timeSlot = 3
        }else if(timeInHr in 19..20){
            delivery_details_7_11_rb.visibility = View.GONE
            delivery_details_11_1_rb.visibility = View.GONE
            delivery_details_1_5_rb.visibility = View.GONE
            delivery_details_5_7_rb.visibility = View.GONE
            delivery_details_7_9_rb.isChecked = true
            timeSlot = 4
        }else{
            delivery_details_time_radiogroup.visibility = View.GONE
            delivery_details_no_slots_available_hint_tv.visibility = View.VISIBLE
            timeSlot = -1
        }

        Log.i(TAG,"TIME SLOT: $timeSlot")
    }

    fun submitOrder(){
        var order = OrderItem()
        order.cartItems = savedCartItems
        order.isPickup = isPickup
        order.preferredDate = Date().time + deliveryDay*DAY_IN_MIL
        order.preferredTimeSlot = timeSlot
        order.address = address!!

        Log.i(TAG,"Order details: $order")

        // submit the order and clear the cart
        firestoreViewModel.submitOrder(order,cartViewModel)
        cartViewModel.deleteAll()
        //addQuotations(order.orderId.toString())
        finish()

    }

    private fun showDialogBox(){
        var alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Place Order?")
            .setMessage("Go ahead and submit order?")
            .setPositiveButton("Yes") { dialog, which ->
                submitOrder()
            }.setNegativeButton("Cancel"){ _,_ ->

            }

        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


}
