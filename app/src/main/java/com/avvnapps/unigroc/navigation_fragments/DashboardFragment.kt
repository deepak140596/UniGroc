package com.avvnapps.unigroc.navigation_fragments


import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.generate_cart.CartItemAdapter
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.generate_cart.SearchItemActivity
import com.avvnapps.unigroc.utils.LocationUtils
import com.avvnapps.unigroc.location_address.SavedAddressesActivity
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.models.RetailerQuotationItem
import com.avvnapps.unigroc.order_status.OrderItemAdapter
import com.avvnapps.unigroc.utils.ApplicationConstants
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.appbar_dashboard.*
import kotlinx.android.synthetic.main.appbar_dashboard.view.*
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment() {

    val TAG = "DASHBOARD_FRAG"
    val SET_ADDRESS_REQUEST_CODE = 100
    lateinit var location: Location
    lateinit var activity : AppCompatActivity
    lateinit var cartViewModel: CartViewModel
    lateinit var firestoreViewModel: FirestoreViewModel
    var savedCartItems : List<CartEntity> = emptyList()

    lateinit var dashboardView: View

    lateinit var recyclerView : RecyclerView
    lateinit var adapter : CartItemAdapter
    var orderList : List<OrderItem> = emptyList()
    var CartItems : List<CartEntity> = emptyList()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as AppCompatActivity
        return inflater.inflate(R.layout.fragment_dashboard,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardView = view
        // get user location and pass to geocoder for address
        LocationUtils(activity).getLocation().observe(activity, Observer { loc : Location? ->
            if(loc != null) {
                location = loc!!
                updateAddress()
            }

        })

        // initialise cart view model
        cartViewModel = ViewModelProviders.of(activity).get(CartViewModel::class.java)
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            updateCartImageView(savedCartItems)
        })

        appbar_dashboard_cart_iv.setOnClickListener {
            startActivity(Intent(activity,ReviewCartActivity::class.java))
        }
        appbar_dashboard_search_ll.setOnClickListener {
            startActivity(Intent(activity,SearchItemActivity::class.java))
        }
        appbar_dashboard_set_delivery_location_tv.setOnClickListener {
            var intent = Intent(activity,SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action",true)
            startActivityForResult(intent,SET_ADDRESS_REQUEST_CODE)
        }

        // initialise firebase view model
        initialiseFirebaseViewModel()
        // set up divider in recycler view
        fragment_dashboard_recycler_view.layoutManager = LinearLayoutManager(activity)
        fragment_dashboard_recycler_view.addItemDecoration(
            DividerItemDecoration(
                fragment_dashboard_recycler_view.context, DividerItemDecoration.VERTICAL
            )
        )
        adapter = CartItemAdapter(activity, CartItems,cartViewModel)
        fragment_dashboard_recycler_view.adapter = adapter

    }

    private fun updateAddress(){
        var address = LocationUtils.getAddress(activity,location.latitude,location.longitude)
        if(address != null) {
            Log.i(TAG, address)
            dashboardView.appbar_dashboard_set_delivery_location_tv.text = address
        }
    }

    private fun updateCartImageView(savedCartItems : List<CartEntity>){
        if(savedCartItems.isNotEmpty()){
            appbar_dashboard_cart_no_tv.text = savedCartItems.size.toString()
            appbar_dashboard_cart_no_tv.visibility = View.VISIBLE
        }else
            appbar_dashboard_cart_no_tv.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SET_ADDRESS_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                location.latitude = data!!.extras.getDouble("latitude")
                location.longitude = data!!.extras.getDouble("longitude")
                updateAddress()
            }
        }
    }

    private fun initialiseFirebaseViewModel(){

        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            CartItems = it
            Log.i(TAG,"Order Size: ${orderList.size}")
            adapter.cartList = CartItems
            adapter.notifyDataSetChanged()
        })
    }


    override fun onResume() {
        super.onResume()
        initialiseFirebaseViewModel()
    }

}