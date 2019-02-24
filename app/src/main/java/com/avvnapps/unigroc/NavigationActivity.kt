package com.avvnapps.unigroc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.avvnapps.unigroc.database.cart.CartEntity
import com.avvnapps.unigroc.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.generate_cart.SearchItemActivity
import com.avvnapps.unigroc.location_address.CreateAddressActivity
import com.avvnapps.unigroc.location_address.LocationUtils
import com.avvnapps.unigroc.location_address.SavedAddressesActivity
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity() {

    var TAG = "NAV_ACTIVITY"

    private lateinit var cartViewModel: CartViewModel
    var cartList: List<CartEntity> = ArrayList<CartEntity>()

    lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        askForPermissions()

        activity_bottom_nav_view.setOnNavigationItemSelectedListener(mOnBottomNavigationItemSelectedListener)

        var user = FirebaseAuth.getInstance().currentUser
        Log.i(TAG,"Name: ${user!!.displayName}  Email: ${user!!.email}  Phone: ${user.phoneNumber}")

        //startActivity(Intent(this@NavigationActivity,CreateAddressActivity::class.java))


    }

    private val mOnBottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        when(it.itemId){
            R.id.bottom_navigation_home ->
                startActivity(Intent(this@NavigationActivity,SavedAddressesActivity::class.java))
            R.id.bottom_navigation_settings ->
                startActivity(Intent(this@NavigationActivity, ReviewCartActivity::class.java))
        }
        return@OnNavigationItemSelectedListener true
    }


    // upload 15 sample items to firebase
    fun addAvailableCartItems(){
        var i = 0
        val db :CollectionReference = FirebaseFirestore.getInstance().collection("available_cart_items")
        do {


            var cartItem = CartEntity(i.toString(),"Item $i","Oils","Kitchen Needs",
                    "https://www.rd.com/wp-content/uploads/2017/11/01_Constipation_Reasons-to-Buy-a-Bottle-of-Castor-Oil-Today_209913937_MaraZe-760x506.jpg",
                    0,45.0,"1kg")
            db.document(cartItem.itemId).set(cartItem)
            i++
        }while(i<15)
    }

    // observe on location and update accordingly
    fun updateLocation(){

        LocationUtils(this).getLocation().observe(this, Observer {loc: Location? ->
            location = loc!!
            Log.i(TAG,"Location: ${location.latitude}  ${location.longitude}")
            Toasty.info(this,"Location: ${location.latitude}  ${location.longitude}").show()
        })
    }

    private fun askForPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            1 ->{

                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permission was granted

                    //isPermissionAcquired = true

                    updateLocation()


                }
                else{
                    // permission was denied
                   // isPermissionAcquired = false
                    Toasty.error(this,"Permission Denied").show()
                    // set empty list view
                }
            }

        }
    }
}
