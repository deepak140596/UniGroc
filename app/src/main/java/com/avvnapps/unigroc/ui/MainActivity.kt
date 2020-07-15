package com.avvnapps.unigroc.ui

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.avvnapps.unigroc.Fonts.CustomTypefaceSpan
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.GeoIpServicesManager.geoIpService
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.GeoIp
import com.avvnapps.unigroc.models.GeoIpResponseModel
import com.avvnapps.unigroc.ui.Activity.*
import com.avvnapps.unigroc.ui.authentication.AuthUiActivity
import com.avvnapps.unigroc.ui.generate_cart.CartItemAdapter
import com.avvnapps.unigroc.ui.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.ui.generate_cart.SearchItemActivity
import com.avvnapps.unigroc.ui.location_address.SavedAddressesActivity
import com.avvnapps.unigroc.utils.*
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    var toolbar: Toolbar? = null
    var navigationView: NavigationView? = null
    private var navMenu: Menu? = null
    private var myOrder: LinearLayout? = null
    private var myReward: LinearLayout? = null
    private var myWallet: LinearLayout? = null
    private var myCart: LinearLayout? = null
    private var ivProfile: ImageView? = null
    private var tvName: TextView? = null
    internal var padding = 0

    var TAG = "Main_ACTIVITY"
    private val SET_ADDRESS_REQUEST_CODE = 100
    lateinit var location: Location

    private val cartViewModel by lazy {
        ViewModelProvider(this).get(CartViewModel::class.java)
    }

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }
    var cartItemAdapter: CartItemAdapter? = null

    var savedCartItems: List<CartEntity> = emptyList()

    val user by lazy { FirebaseAuth.getInstance().currentUser }

    private lateinit var gpsUtils: GpsUtils

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Location Permissions
        askForPermissions()

        gpsUtils = GpsUtils(this)

        getGeoIpAddress()

        //inflate Navigation Drawer
        inflateNavDrawer()
        //inflate Slides
        inflateSlider()
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            updateCartImageView(savedCartItems)
        })

        // initialise firebase view model
        initialiseFirebaseViewModel()

        handleNetworkChanges()
        setOnclickListeners()
    }

    private fun setOnclickListeners() {

        appbar_dashboard_cart_iv.setOnClickListener {
            startActivity(Intent(this, ReviewCartActivity::class.java))
        }
        home_search_ll.setOnClickListener {
            startActivity(Intent(this, SearchItemActivity::class.java))
        }
        appbar_dashboard_set_delivery_location_tv.setOnClickListener {
            val intent = Intent(this, SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action", true)
            startActivityForResult(intent, SET_ADDRESS_REQUEST_CODE)
        }


        view_all_topselling.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(this, Products::class.java)
            )
        })
    }

    private fun setupRecyclerView() {
        cartItemAdapter = CartItemAdapter(this@MainActivity, savedCartItems, cartViewModel)
        top_selling_recycler.apply {
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = cartItemAdapter
        }

        rv_deal_of_the_day.apply {
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = cartItemAdapter
        }


        shimmerTopSelling.stopShimmer()
        shimmerTopSelling.animateVisibility(View.GONE)
        shimmerDealDay.stopShimmer()
        shimmerDealDay.animateVisibility(View.GONE)
        top_selling_recycler.show()
        rv_deal_of_the_day.show()

        cartItemAdapter?.setOnItemClickListener(object : CartItemAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                val cartItem: CartEntity = cartItemAdapter?.getItem(pos) as CartEntity
                val intent = Intent(this@MainActivity, IndividualProduct::class.java)
                intent.putExtra("product", cartItem)
                startActivity(intent)

            }
        })
    }

    private fun getGeoIpAddress() {

        val ipApiService = geoIpService
        ipApiService.getGeoIp().enqueue(object : Callback<GeoIpResponseModel?> {
            override fun onResponse(
                call: Call<GeoIpResponseModel?>,
                response: Response<GeoIpResponseModel?>
            ) {
                try {
                    Log.d(TAG, response.toString())
                    Log.d(TAG, response.body().toString())
                    val countryName: String? = response.body()!!.countryName
                    val currency: String? = response.body()!!.currency
                    val country: String? = response.body()!!.country
                    val latitude: Double = response.body()!!.latitude
                    val longtidue: Double = response.body()!!.longitude
                    val isp: String? = response.body()!!.ip
                    val GeoIpValues = GeoIp(
                        countryName!!,
                        currency!!,
                        country!!,
                        latitude,
                        longtidue,
                        isp.toString()
                    )
                    SharedPreferencesDB.savePreferredGeoIp(this@MainActivity, GeoIpValues)
                    // set up divider in recycler view
                    setupRecyclerView()

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<GeoIpResponseModel?>, t: Throwable) {
                // Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_SHORT).show()
                Log.e(TAG, t.toString())
            }
        })
    }

    companion object {
        const val ANIMATION_DURATION = 1000.toLong()
    }

    /**
     * Observe network changes i.e. Internet Connectivity
     */
    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer { isConnected ->
            if (!isConnected) {
                textViewNetworkStatus.text = getString(R.string.text_no_connectivity)
                networkStatusLayout.apply {
                    show()
                    setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                }
            } else {
                initialiseFirebaseViewModel()
                textViewNetworkStatus.text = getString(R.string.text_connectivity)
                networkStatusLayout.apply {
                    setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                    animate()
                        .alpha(1f)
                        .setStartDelay(ANIMATION_DURATION)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                hide()
                            }
                        })
                }
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.nav_shop_now -> {
            }
            R.id.nav_my_profile -> {
                startActivity(
                    Intent(this, ProfileActivity::class.java)
                )

            }
            R.id.nav_wishlist -> {
                startActivity(
                    Intent(this, Wishlist::class.java)
                )
            }
            R.id.nav_aboutus -> {

            }
            R.id.nav_policy -> {

            }
            R.id.nav_review -> {
                this.openAppInGooglePlay()
            }
            R.id.nav_contact -> {
                startActivity(
                    Intent(this, ContactUs::class.java)
                )
            }
            R.id.nav_share -> {
                shareApp()
            }
            R.id.nav_logout -> {
                signOut()
            }
            R.id.nav_powerd -> {
                // stripUnderlines(textView);
                val url = "https://www.google.com/"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
                finish()
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun updateCartImageView(savedCartItems: List<CartEntity>) {
        if (savedCartItems.isNotEmpty()) {
            appbar_dashboard_cart_no_tv.text = savedCartItems.size.toString()
            appbar_dashboard_cart_no_tv.visibility = View.VISIBLE
        } else
            appbar_dashboard_cart_no_tv.visibility = View.GONE
    }


    //Image Slider
    private fun inflateSlider() {
        val imageSlider = findViewById<ImageSlider>(R.id.image_slider)

        val listURL = ArrayList<SlideModel>()
        listURL.add(SlideModel("https://i.imgur.com/F142xAr.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/rjUxZOJ.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/B3vqL4T.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/DeRFbNz.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/tuQ03J9.jpg"))


        imageSlider.setImageList(listURL, true)

    }


    // observe on location and update accordingly
    private fun updateLocation() {

        val addressData = SharedPreferencesDB.getSavedAddress(this)

        if (latitude == 0.0 && longitude == 0.0) {

            if (addressData == null) {
                appbar_dashboard_set_delivery_location_tv.text = "Select Address"
            } else {
                val address =
                    LocationUtils.getAddress(this, addressData.latitude, addressData.longitude)
                if (address != null) {
                    Log.i(TAG, address)
                    appbar_dashboard_set_delivery_location_tv.text = address

                }
            }

        } else {
            val address = LocationUtils.getAddress(this, latitude, longitude)
            if (address != null) {
                Log.i(TAG, address)
                appbar_dashboard_set_delivery_location_tv.text = address

            }
        }


    }

    private fun askForPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    //getLocation()
                    updateLocation()
                } else {
                    // permission was denied
                    // isPermissionAcquired = false
                    Toasty.error(this, "Permission Denied").show()
                }
            }

        }
    }

    private fun getLocation() {
        gpsUtils.getLatLong { lat, long ->
            Log.i(TAG, "location is $lat + $long")
            try {
                val address = LocationUtils.getAddress(this, lat, long)
                if (address != null) {
                    Log.i(TAG, address)
                    appbar_dashboard_set_delivery_location_tv.text = address
                } else {
                    appbar_dashboard_set_delivery_location_tv.text = "Select Address"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }


    private fun inflateNavDrawer() {
        //set Custom toolbar to activity -----------------------------------------------------------
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        for (i in 0 until toolbar!!.childCount) {
            val view = toolbar!!.getChildAt(i)

            if (view is TextView) {

                val myCustomFont = Typeface.createFromAsset(assets, "Fonts/Bold.otf")
                view.typeface = myCustomFont
            }

        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView = findViewById<NavigationView>(R.id.nav_view)
        val m = navigationView!!.menu
        for (i in 0 until m.size()) {
            val mi = m.getItem(i)

            //for aapplying a font to subMenu ...
            val subMenu = mi.subMenu
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi)
        }

        navigationView!!.setNavigationItemSelectedListener(this)
        navMenu = navigationView!!.menu
        val header = (findViewById<NavigationView>(R.id.nav_view)).getHeaderView(0)
        ivProfile = header.findViewById(R.id.iv_header_img) as CircleImageView

        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.user)
            .error(R.drawable.user)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .dontAnimate()
            .dontTransform()

        Glide.with(this)
            .applyDefaultRequestOptions(options)
            .load(user!!.photoUrl)
            .into(ivProfile as CircleImageView)

        tvName = header.findViewById(R.id.tv_header_name) as TextView
        tvName!!.text = user!!.displayName.toString()
        myOrder = header.findViewById(R.id.my_orders) as LinearLayout
        myReward = header.findViewById(R.id.my_reward) as LinearLayout
        myWallet = header.findViewById(R.id.my_wallet) as LinearLayout
        myCart = header.findViewById(R.id.my_cart) as LinearLayout

        myOrder!!.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(this, Order::class.java)
            )
        })

        myCart!!.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ReviewCartActivity::class.java))
        })

    }

    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "Fonts/Bold.otf")
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
        gpsUtils.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SET_ADDRESS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                latitude = data!!.extras!!.getDouble("latitude")
                longitude = data.extras!!.getDouble("longitude")
                updateLocation()
                //getLocation()
            }
        }
    }

    private fun initialiseFirebaseViewModel() {
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            savedCartItems = it
            Log.i(TAG, "Order Size: ${savedCartItems.size}")
            cartItemAdapter?.cartList = savedCartItems
            cartItemAdapter?.notifyDataSetChanged()
        })
    }

    private fun signOut() {

        val builder = AlertDialog.Builder(this)
        val itemView = LayoutInflater.from(this).inflate(R.layout.layout_sign_out, null)

        val btn_sign_out = itemView.findViewById<View>(R.id.btn_sign_out) as Button
        val btn_back = itemView.findViewById<View>(R.id.btn_no) as Button
        val btn_cancelled = itemView.findViewById<View>(R.id.btn_cancel) as ImageView

        builder.setView(itemView)
        val shows = builder.create()

        btn_sign_out.setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    startActivity(
                        Intent(this, AuthUiActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    finish()
                }
        }

        btn_cancelled.setOnClickListener {
            shows.dismiss()
        }

        btn_back.setOnClickListener {
            shows.dismiss()
        }
        shows.show()
    }

    private fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Hi friends i am using . http://play.google.com/store/apps/details?id=$packageName APP"
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }


    override fun onResume() {
        super.onResume()
        initialiseFirebaseViewModel()
        shimmerTopSelling.startShimmer()
        shimmerDealDay.startShimmer()
    }

    override fun onPause() {
        shimmerTopSelling.stopShimmer()
        shimmerDealDay.stopShimmer()
        super.onPause()
    }

}
