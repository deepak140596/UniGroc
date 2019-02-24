package com.avvnapps.unigroc.location_address

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_saved_addresses.*

class SavedAddressesActivity : AppCompatActivity() {

    var firestoreViewModel: FirestoreViewModel ?= null
    lateinit var savedAddresses : List<AddressItem>
    lateinit var adapter : AddressItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_addresses)

        // set up divider in recycler view
        activity_saved_add_rv.layoutManager = LinearLayoutManager(this)
        activity_saved_add_rv.addItemDecoration(
            DividerItemDecoration(
                activity_saved_add_rv.context,DividerItemDecoration.VERTICAL
            )
        )

        // initialise firestore view model and adapter
        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        savedAddresses = ArrayList<AddressItem>()
        adapter = AddressItemAdapter(this,savedAddresses,firestoreViewModel!!)
        activity_saved_add_rv.adapter = adapter



        activity_saved_add_fab.setOnClickListener {
            startActivity(Intent(this,CreateAddressActivity::class.java))
        }

        getSavedAddresses()
    }

    fun getSavedAddresses(){
        firestoreViewModel!!.getSavedAddresses().observe(this, Observer {
            savedAddresses = it
            adapter.addressList = savedAddresses
            adapter.notifyDataSetChanged()
        })
    }
}
