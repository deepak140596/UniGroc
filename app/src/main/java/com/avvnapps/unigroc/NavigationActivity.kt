package com.avvnapps.unigroc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.avvnapps.unigroc.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.navigation_fragments.DashboardFragment
import com.avvnapps.unigroc.utils.LocationUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity() {

    var TAG = "NAV_ACTIVITY"
    lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        askForPermissions()

        activity_bottom_nav_view.setOnNavigationItemSelectedListener(mOnBottomNavigationItemSelectedListener)

        val user = FirebaseAuth.getInstance().currentUser
        Log.i(TAG, "Name: ${user!!.displayName}  Email: ${user.email}  Phone: ${user.phoneNumber}")


    }

    private val mOnBottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {

        when(it.itemId){
            R.id.bottom_navigation_home ->{
                //startActivity(Intent(this@NavigationActivity,SavedAddressesActivity::class.java))
                startFragment(DashboardFragment())
            }

            R.id.bottom_navigation_settings ->
                startActivity(Intent(this@NavigationActivity, ReviewCartActivity::class.java))
        }

        return@OnNavigationItemSelectedListener true
    }

    fun startFragment(fragment : Fragment){
        if(fragment != null){
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.activity_nav_frame_layout,fragment,"").commit()
        }
    }

    // upload 15 sample items to firebase
    fun addAvailableCartItems(){
        var i = 0
        val db :CollectionReference = FirebaseFirestore.getInstance().collection("available_cart_items")
        do {


            val cartItem = CartEntity(
                i.toString(), "Item $i", "Oils", "Kitchen Needs",
                "https://www.rd.com/wp-content/uploads/2017/11/01_Constipation_Reasons-to-Buy-a-Bottle-of-Castor-Oil-Today_209913937_MaraZe-760x506.jpg",
                0, 45.0, "1kg"
            )
            db.document(cartItem.itemId).set(cartItem)
            i++
        }while(i<15)
    }

    // observe on location and update accordingly
    private fun updateLocation() {

        LocationUtils(this).getLocation().observe(this, Observer { loc: Location? ->


            if(loc != null) {
                location = loc
                Log.i(TAG,"Location: ${location.latitude}  ${location.longitude}")
                startFragment(DashboardFragment())
            }
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
