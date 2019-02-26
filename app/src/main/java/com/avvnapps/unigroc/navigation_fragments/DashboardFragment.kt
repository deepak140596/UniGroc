package com.avvnapps.unigroc.navigation_fragments

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.cart.CartEntity
import com.avvnapps.unigroc.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.generate_cart.SearchItemActivity
import com.avvnapps.unigroc.location_address.LocationUtils
import com.avvnapps.unigroc.viewmodel.CartViewModel
import kotlinx.android.synthetic.main.appbar_dashboard.*
import kotlinx.android.synthetic.main.appbar_dashboard.view.*

class DashboardFragment : Fragment() {

    val TAG = "DASHBOARD_FRAG"
    lateinit var location: Location
    lateinit var activity : AppCompatActivity
    lateinit var cartViewModel: CartViewModel
    lateinit var savedCartItems : List<CartEntity>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as AppCompatActivity
        return inflater.inflate(R.layout.fragment_dashboard,container,false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get user location and pass to geocoder for address
        LocationUtils(activity).getLocation().observe(activity, Observer {loc ->
            location = loc
            var address = LocationUtils.getAddress(activity,location.latitude,location.longitude)
            if(address != null) {
                Log.i(TAG, address)
                view.appbar_dashboard_set_delivery_location_tv.text = address
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

    }

    private fun updateCartImageView(savedCartItems : List<CartEntity>){
        if(savedCartItems.isNotEmpty()){
            appbar_dashboard_cart_no_tv.text = savedCartItems.size.toString()
            appbar_dashboard_cart_no_tv.visibility = View.VISIBLE
        }else
            appbar_dashboard_cart_no_tv.visibility = View.GONE
    }

}