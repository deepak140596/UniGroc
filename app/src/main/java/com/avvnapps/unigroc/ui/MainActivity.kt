package com.avvnapps.unigroc.ui

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
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
import android.widget.*
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
import com.androidfung.geoip.GeoIpService
import com.androidfung.geoip.ServicesManager
import com.androidfung.geoip.model.GeoIpResponseModel
import com.avvnapps.unigroc.Activity.*
import com.avvnapps.unigroc.Fonts.CustomTypefaceSpan
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.models.GeoIp
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    var toolbar: Toolbar? = null
    var navigationView: NavigationView? = null
    private var nav_menu: Menu? = null
    internal var My_Order: LinearLayout? = null
    internal var My_Reward: LinearLayout? = null
    internal var My_Walllet: LinearLayout? = null
    internal var My_Cart: LinearLayout? = null
    private var iv_profile: ImageView? = null
    private var tv_name: TextView? = null
    internal var padding = 0

    var TAG = "Main_ACTIVITY"
    val SET_ADDRESS_REQUEST_CODE = 100
    lateinit var location: Location
    lateinit var cartViewModel: CartViewModel
    lateinit var firestoreViewModel: FirestoreViewModel
    var savedCartItems: List<CartEntity> = emptyList()
    lateinit var adapter: CartItemAdapter
    var user = FirebaseAuth.getInstance().currentUser

    private lateinit var gpsUtils: GpsUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Location Permissions
        askForPermissions()

        gpsUtils = GpsUtils(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        val crashlytics = FirebaseCrashlytics.getInstance()

        crashlytics.log("my message")

// To log a message to a crash report, use the following syntax:

        val ipApiService: GeoIpService = ServicesManager.getGeoIpService()
        ipApiService.geoIp.enqueue(object : Callback<GeoIpResponseModel?> {
            override fun onResponse(
                call: Call<GeoIpResponseModel?>,
                response: Response<GeoIpResponseModel?>
            ) {
                val countryName: String = response.body()!!.countryName
                val currency: String = response.body()!!.currency
                val country: String = response.body()!!.country
                val latitude: Double = response.body()!!.latitude
                val longtidue: Double = response.body()!!.longitude
                val isp: String = response.body()!!.ip

                var GeoIpValues = GeoIp(
                    countryName,
                    currency,
                    country,
                    latitude,
                    longtidue,
                    isp
                )
                crashlytics.log("E/TAG: Country Currency : $currency")
                Log.e(TAG, "Country Currency : $currency")
                SharedPreferencesDB.savePreferredGeoIp(this@MainActivity, GeoIpValues)

            }

            override fun onFailure(call: Call<GeoIpResponseModel?>?, t: Throwable) {
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_SHORT).show()
            }


        })

        //inflate Navigation Drawer
        inflateNavDrawer()
        init()
        //inflate Slides
        inflateSLider()
        // initialise cart view model
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartViewModel.cartList.observe(this, Observer {
            savedCartItems = it
            updateCartImageView(savedCartItems)
        })
        Log.i(
            TAG,
            "Name: ${user!!.displayName}  Email: ${user!!.email}  Phone: ${user!!.phoneNumber}"
        )

        getLocation()

        // get user location and pass to geocoder for address
        /*  LocationUtils(this).getLocation().observe(this, Observer { loc: Location? ->
              if (loc != null) {
                  location = loc
                  //updateAddress()
                  getLocation()
              }

          })*/

        // initialise firebase view model
        initialiseFirebaseViewModel()
        // set up divider in recycler view
        top_selling_recycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv_deal_of_the_day.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        adapter = CartItemAdapter(
            this,
            savedCartItems,
            cartViewModel
        )
        top_selling_recycler.adapter = adapter
        rv_deal_of_the_day.adapter = adapter
        adapter.setOnItemClickListener(object : CartItemAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                val cartItem: CartEntity = adapter.getItem(pos) as CartEntity
                val intent = Intent(this@MainActivity, IndividualProduct::class.java)
                intent.putExtra("product", cartItem)
                startActivity(intent)

            }
        })

        view_all_topselling.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(this, Products::class.java)
            )
        })

        handleNetworkChanges()


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
        var id = item.itemId
        if (id == R.id.nav_shop_now) {
        } else if (id == R.id.nav_my_profile) {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )

        } else if (id == R.id.nav_wishlist) {
            startActivity(
                Intent(this, Wishlist::class.java)
            )
        } else if (id == R.id.nav_aboutus) {

        } else if (id == R.id.nav_policy) {

        } else if (id == R.id.nav_review) {
            reviewOnApp()
        } else if (id == R.id.nav_contact) {
            startActivity(
                Intent(this, ContactUs::class.java)
            )
        } else if (id == R.id.nav_share) {
            shareApp()
        } else if (id == R.id.nav_logout) {
            signOut()
        } else if (id == R.id.nav_powerd) {
            // stripUnderlines(textView);
            val url = "https://www.google.com/"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            finish()
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

    private fun init() {
        appbar_dashboard_cart_iv.setOnClickListener {
            startActivity(Intent(this, ReviewCartActivity::class.java))
        }
        home_search_ll.setOnClickListener {
            startActivity(Intent(this, SearchItemActivity::class.java))
        }
        appbar_dashboard_set_delivery_location_tv.setOnClickListener {
            val intent = Intent(this, SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action", true)
            startActivity(intent)
            startActivityForResult(intent, SET_ADDRESS_REQUEST_CODE)
        }


    }

    //Image Slider
    private fun inflateSLider() {
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
    fun updateLocation() {

        LocationUtils(this).getLocation().observe(this, Observer { loc: Location? ->

            if (loc != null) {
                location = loc
                Log.i(TAG, "Location: ${location.latitude}  ${location.longitude}")
                gpsUtils.getLatLong { lat, long ->

                    val address = LocationUtils.getAddress(this, lat, long)
                    if (address != null) {
                        Log.i(TAG, address)
                        appbar_dashboard_set_delivery_location_tv.text = address

                    }
                }

            }
        })
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

                    //isPermissionAcquired = true
                    gpsUtils.onProgressUpdate = { show ->
                        // updateLocation()

                    }
                    getLocation()

                } else {
                    // permission was denied
                    // isPermissionAcquired = false
                    Toasty.error(this, "Permission Denied").show()
                    // set empty list view
                }
            }

        }
    }

    private fun getLocation() {
        gpsUtils.getLatLong { lat, long ->
            Log.i(TAG, "location is $lat + $long")
            val address = LocationUtils.getAddress(this, lat, long)
            if (address != null) {
                Log.i(TAG, address)
                appbar_dashboard_set_delivery_location_tv.text = address
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
        nav_menu = navigationView!!.menu
        val header = (findViewById<NavigationView>(R.id.nav_view)).getHeaderView(0)
        iv_profile = header.findViewById(R.id.iv_header_img) as CircleImageView

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
            .into(iv_profile as CircleImageView)

        tv_name = header.findViewById(R.id.tv_header_name) as TextView
        tv_name!!.text = user!!.displayName.toString()
        My_Order = header.findViewById(R.id.my_orders) as LinearLayout
        My_Reward = header.findViewById(R.id.my_reward) as LinearLayout
        My_Walllet = header.findViewById(R.id.my_wallet) as LinearLayout
        My_Cart = header.findViewById(R.id.my_cart) as LinearLayout

        My_Order!!.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(this, Order::class.java)
            )
        })

        My_Cart!!.setOnClickListener(View.OnClickListener {
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
                //  location.latitude = data!!.extras!!.getDouble("latitude")
                // location.longitude = data!!.extras!!.getDouble("longitude")
                //updateLocation()
                getLocation()
            }
        }
    }

    private fun initialiseFirebaseViewModel() {

        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            savedCartItems = it
            Log.i(TAG, "Order Size: ${savedCartItems.size}")
            adapter.cartList = savedCartItems
            adapter.notifyDataSetChanged()
        })
    }

    fun signOut() {

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

    fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Hi friends i am using ." + " http://play.google.com/store/apps/details?id=" + packageName + " APP"
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }


    fun reviewOnApp() {
        val uri = Uri.parse("market://details?id=" + packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)
                )
            )
        }

    }

    override fun onResume() {
        super.onResume()
        initialiseFirebaseViewModel()
    }

}
