package com.avvnapps.unigroc.ui.location_address

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.AddressItem
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.activity_saved_addresses.*

class SavedAddressesActivity : AppCompatActivity() {

    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }
    lateinit var savedAddresses: List<AddressItem>
    lateinit var adapter: AddressItemAdapter
    var isSelectableAction = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_addresses)

        // check if the activity is opened to select address
        isSelectableAction = intent.getBooleanExtra("is_selectable_action", false)

        // set up divider in recycler view
        activity_saved_add_rv.layoutManager = LinearLayoutManager(this)
        activity_saved_add_rv.addItemDecoration(
            DividerItemDecoration(
                activity_saved_add_rv.context,DividerItemDecoration.VERTICAL
            )
        )

        savedAddresses = ArrayList<AddressItem>()
        adapter = AddressItemAdapter(
            this@SavedAddressesActivity as AppCompatActivity,
            savedAddresses,
            firestoreViewModel,
            isSelectableAction
        )
        activity_saved_add_rv.adapter = adapter

        activity_saved_add_fab.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CreateAddressActivity::class.java
                )
            )
        }
        getSavedAddresses()
    }

    private fun getSavedAddresses() {
        firestoreViewModel.getSavedAddresses().observe(this, Observer {
            savedAddresses = it
            adapter.addressList = savedAddresses
            adapter.notifyDataSetChanged()
        })
    }
}
