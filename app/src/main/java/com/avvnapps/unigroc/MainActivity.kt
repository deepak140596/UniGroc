package com.avvnapps.unigroc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.avvnapps.unigroc.Activity.EmptyCart
import com.avvnapps.unigroc.Font.CustomTypefaceSpan
import com.avvnapps.unigroc.Fragments.HomeFragment
import com.avvnapps.unigroc.generate_cart.CartItemAdapter
import com.avvnapps.unigroc.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.generate_cart.SearchItemActivity
import com.avvnapps.unigroc.location_address.SavedAddressesActivity
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.utils.LocationUtils
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.navigation.NavigationView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var navigationView: NavigationView? = null
    internal var padding = 0

    var TAG = "Main_ACTIVITY"
    val SET_ADDRESS_REQUEST_CODE = 100
    lateinit var location: Location
    lateinit var cartViewModel: CartViewModel
    lateinit var firestoreViewModel: FirestoreViewModel
    var savedCartItems : List<CartEntity> = emptyList()
    lateinit var adapter : CartItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Location Permissions
        askForPermissions()

        //inflate Navigation Drawer
        inflateNavDrawer();
        init()
        //inflate Slides
        inflateSLider()
        // initialise cart view model
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            updateCartImageView(savedCartItems)
        })

        // initialise firebase view model
        initialiseFirebaseViewModel()
        // set up divider in recycler view
        top_selling_recycler.layoutManager = LinearLayoutManager(this)

        adapter = CartItemAdapter(this, savedCartItems,cartViewModel)
        top_selling_recycler.adapter = adapter


    }
    private fun updateCartImageView(savedCartItems : List<CartEntity>){
        if(savedCartItems.isNotEmpty()){
            appbar_dashboard_cart_no_tv.text = savedCartItems.size.toString()
            appbar_dashboard_cart_no_tv.visibility = View.VISIBLE
        }else
            appbar_dashboard_cart_no_tv.visibility = View.GONE
    }

    private fun init(){
        appbar_dashboard_cart_iv.setOnClickListener {
            startActivity(Intent(this, ReviewCartActivity::class.java))
        }
        home_search_ll.setOnClickListener {
            startActivity(Intent(this, SearchItemActivity::class.java))
        }
        appbar_dashboard_set_delivery_location_tv.setOnClickListener {
            var intent = Intent(this, SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action",true)
            startActivityForResult(intent,SET_ADDRESS_REQUEST_CODE)
        }

        val cart = findViewById(R.id.appbar_dashboard_cart_rl) as RelativeLayout
        cart.setOnClickListener {
            startActivity(Intent(this,EmptyCart::class.java))
        }
    }

    private fun inflateSLider() {
        val imageSlider = findViewById<ImageSlider>(R.id.image_slider)

        val listURL = ArrayList<SlideModel>()
        listURL.add(SlideModel("https://i.imgur.com/F142xAr.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/rjUxZOJ.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/B3vqL4T.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/DeRFbNz.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/tuQ03J9.jpg"))


        imageSlider.setImageList(listURL,true)

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



    private fun inflateNavDrawer(){
        //set Custom toolbar to activity -----------------------------------------------------------
        toolbar = findViewById(R.id.toolbar)
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
        drawer.addDrawerListener(toggle)
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

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SET_ADDRESS_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                location.latitude = data!!.extras.getDouble("latitude")
                location.longitude = data!!.extras.getDouble("longitude")
                updateLocation()
            }
        }
    }

    private fun initialiseFirebaseViewModel(){

        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            savedCartItems = it
            Log.i(TAG,"Order Size: ${savedCartItems.size}")
            adapter.cartList = savedCartItems
            adapter.notifyDataSetChanged()
        })
    }


    override fun onResume() {
        super.onResume()
        initialiseFirebaseViewModel()
    }

}
