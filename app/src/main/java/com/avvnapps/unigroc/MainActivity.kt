package com.avvnapps.unigroc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.avvnapps.unigroc.Activity.EmptyCart
import com.avvnapps.unigroc.Font.CustomTypefaceSpan
import com.avvnapps.unigroc.Fragments.HomeFragment
import com.avvnapps.unigroc.utils.LocationUtils
import com.google.android.material.navigation.NavigationView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.app_bar_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var navigationView: NavigationView? = null
    internal var padding = 0

    var TAG = "Main_ACTIVITY"
    lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Location Permissions
        askForPermissions()

        //inflate Navigation Drawer
        inflateNavDrawer();
        //start Fragment
        startFragment(HomeFragment())


        val cart = findViewById(R.id.appbar_dashboard_cart_rl) as RelativeLayout
        cart.setOnClickListener {
            startActivity(Intent(this,EmptyCart::class.java))
        }
    }

    // observe on location and update accordingly
    fun updateLocation(){

        LocationUtils(this).getLocation().observe(this, Observer { loc: Location? ->

            if(loc != null) {
                location = loc!!
                Log.i(TAG,"Location: ${location.latitude}  ${location.longitude}")
                var address = LocationUtils.getAddress(this,location.latitude,location.longitude)
                if(address != null) {
                    Log.i(TAG, address)
                    try {
                        appbar_dashboard_set_delivery_location_tv.text = address

                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun askForPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
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


    fun startFragment(fragment : Fragment){
        if(fragment != null){
            var fragmentManager =supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.contentPanel,fragment,"Home_fragment")
                .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }

    }

    private fun inflateNavDrawer(){
        //set Custom toolbar to activity -----------------------------------------------------------
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setPadding(padding, toolbar!!.getPaddingTop(), padding, toolbar!!.getPaddingBottom())
        setSupportActionBar(toolbar)
        for (i in 0 until toolbar!!.getChildCount()) {
            val view = toolbar!!.getChildAt(i)

            if (view is TextView) {

                val myCustomFont = Typeface.createFromAsset(assets, "Font/Bold.otf")
                view.typeface = myCustomFont
            }

        }
        supportActionBar!!.setTitle(resources.getString(R.string.app_name))

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        navigationView = findViewById(R.id.nav_view) as NavigationView
        val m = navigationView!!.getMenu()
        for (i in 0 until m.size()) {
            val mi = m.getItem(i)

            //for aapplying a font to subMenu ...
            val subMenu = mi.getSubMenu()
            if (subMenu != null && subMenu!!.size() > 0) {
                for (j in 0 until subMenu!!.size()) {
                    val subMenuItem = subMenu!!.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi)
        }



    }

    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "Font/Bold.otf")
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(
            CustomTypefaceSpan("", font),
            0,
            mNewTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        mi.title = mNewTitle
    }

}
