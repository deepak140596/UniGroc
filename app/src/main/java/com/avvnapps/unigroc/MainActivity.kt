package com.avvnapps.unigroc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.avvnapps.unigroc.Activity.*
import com.avvnapps.unigroc.Font.CustomTypefaceSpan
import com.avvnapps.unigroc.authentication.AuthUiActivity
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
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception

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
        Log.i(
            TAG,
            "Name: ${user!!.displayName}  Email: ${user!!.email}  Phone: ${user!!.phoneNumber}"
        )


        // initialise firebase view model
        initialiseFirebaseViewModel()
        // set up divider in recycler view
        top_selling_recycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv_deal_of_the_day.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        adapter = CartItemAdapter(this, savedCartItems, cartViewModel)
        top_selling_recycler.adapter = adapter
        rv_deal_of_the_day.adapter = adapter
        adapter.setOnItemClickListener(object : CartItemAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                val cartItem: CartEntity = adapter.getItem(pos) as CartEntity;
                if (cartItem == null)
                    return;
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
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
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
            var intent = Intent(this, SavedAddressesActivity::class.java)
            intent.putExtra("is_selectable_action", true)
            startActivityForResult(intent, SET_ADDRESS_REQUEST_CODE)
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


        imageSlider.setImageList(listURL, true)

    }


    // observe on location and update accordingly
    fun updateLocation() {

        LocationUtils(this).getLocation().observe(this, Observer { loc: Location? ->

            if (loc != null) {
                location = loc!!
                Log.i(TAG, "Location: ${location.latitude}  ${location.longitude}")
                var address = LocationUtils.getAddress(this, location.latitude, location.longitude)
                if (address != null) {
                    Log.i(TAG, address)
                    try {
                        appbar_dashboard_set_delivery_location_tv.text = address

                    } catch (e: Exception) {
                        e.printStackTrace()
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

                    updateLocation()

                } else {
                    // permission was denied
                    // isPermissionAcquired = false
                    Toasty.error(this, "Permission Denied").show()
                    // set empty list view
                }
            }

        }
    }


    @SuppressLint("ResourceAsColor")
    private fun inflateNavDrawer() {
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

        val headerView = navigationView!!.getHeaderView(0)
        navigationView!!.getBackground()
            .setColorFilter(R.color.color_grey, PorterDuff.Mode.MULTIPLY)
        navigationView!!.setNavigationItemSelectedListener(this)
        nav_menu = navigationView!!.getMenu()
        val header = (findViewById(R.id.nav_view) as NavigationView).getHeaderView(0)
        iv_profile = header.findViewById(R.id.iv_header_img) as ImageView
        tv_name = header.findViewById(R.id.tv_header_name) as TextView
        tv_name!!.setText(user!!.displayName)
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
        if (requestCode == SET_ADDRESS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                location.latitude = data!!.extras.getDouble("latitude")
                location.longitude = data!!.extras.getDouble("longitude")
                updateLocation()
            }
        }
    }

    private fun initialiseFirebaseViewModel() {

        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            savedCartItems = it
            Log.i(TAG, "Order Size: ${savedCartItems.size}")
            adapter.cartList = savedCartItems
            adapter.notifyDataSetChanged()
        })
    }

    fun signOut() {
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
