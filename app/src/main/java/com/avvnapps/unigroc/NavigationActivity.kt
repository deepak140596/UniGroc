package com.avvnapps.unigroc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import com.avvnapps.unigroc.database.cart.CartEntity
import com.avvnapps.unigroc.generate_cart.ReviewCartActivity
import com.avvnapps.unigroc.generate_cart.SearchItemActivity
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity() {

    var TAG = "NAV_ACTIVITY"

    private lateinit var cartViewModel: CartViewModel
    var cartList: List<CartEntity> = ArrayList<CartEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        activity_bottom_nav_view.setOnNavigationItemSelectedListener(mOnBottomNavigationItemSelectedListener)

        var user = FirebaseAuth.getInstance().currentUser
        Log.i(TAG,"Name: ${user!!.displayName}  Email: ${user!!.email}  Phone: ${user.phoneNumber}")


        /*cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        cartViewModel.cartList.observe(this, Observer {
            cartList = it!!
            Log.i(TAG,"CartList Size: "+cartList.size)
            Log.i(TAG,"First Item Name: "+cartList.get(0).name)
            Log.i(TAG,"Qty: "+cartList.get(0).quantity)
        })

        var cartEntity = CartEntity("A","Sample","a","a","a",2,23.8,"1kg")

        cartViewModel.insert(cartEntity)
        cartViewModel.setQuantity("A",4)


        val firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        firestoreViewModel.getAvailableCartItems().observe(this,Observer{articles->
            Log.d(TAG,"Size: "+articles!!.size)
            Log.d(TAG,"Firse element ${articles!!.get(0).name}")
        })*/

        addAvailableCartItems()
    }

    private val mOnBottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        when(it.itemId){
            R.id.bottom_navigation_home ->
                startActivity(Intent(this@NavigationActivity,SearchItemActivity::class.java))
            R.id.bottom_navigation_settings ->
                startActivity(Intent(this@NavigationActivity, ReviewCartActivity::class.java))
        }
        return@OnNavigationItemSelectedListener true
    }


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
}
