package com.avvnapps.unigroc.location_address

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_address.*
import java.util.*

class CreateAddressActivity : AppCompatActivity() {
    val TAG = "CREATE_ADDRESS"
    val PLACE_PICKER_REQUEST = 1
    var latitutde : Double ?= null
    var longitude : Double ?= null
    var firestoreViewModel : FirestoreViewModel ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_address)

        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)

        create_address_set_location_btn.setOnClickListener {
            createPlacePicker()
        }

        create_address_done_fab.setOnClickListener {
            if(isFormValid()){
                Toasty.success(this@CreateAddressActivity,"Form Valid!").show()
                var addressItem = AddressItem(Date().time,
                    create_address_name_edit_text.text.toString(),
                    create_address_house_name_edit_text.text.toString(),
                    create_address_locality_edit_text.text.toString(),
                    create_address_landmark_edit_text.text.toString(),
                    latitutde!!,longitude!!)

                firestoreViewModel!!.saveAddressToFirebase(addressItem)
                finish()
            }
        }

    }

    fun isFormValid() : Boolean{

        if(create_address_house_name_edit_text.text.toString().trim().length == 0){
            create_address_house_name_input_layout.error = "Enter House Name"
            return false
        }
        if(create_address_locality_edit_text.text.toString().trim().length == 0){
            create_address_locality_input_layout.error = "Enter Locality"
            return false
        }
        if(create_address_landmark_edit_text.text.toString().trim().length == 0){
            create_address_landmark_input_layout.error = "Enter Landmark"
            return false
        }
        if(create_address_name_edit_text.text.toString().trim().length == 0){
            create_address_name_input_layout.error = "Save address as"
            return false
        }
        if(latitutde == null || longitude == null){
            Toasty.error(this,"Please select location on Map!").show()
            return false
        }
        return true

    }

    fun createPlacePicker(){

        val builder = PlacePicker.IntentBuilder()


        try {
            Log.d(TAG, "opening startActivityforResult")
            startActivityForResult(builder.build(this@CreateAddressActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {

                val place = PlacePicker.getPlace(this,data)
                val latLng = place.latLng
                Log.d(TAG, "LatLng: $latLng")
                latitutde = latLng.latitude
                longitude = latLng.longitude

                create_address_is_location_set_cb.isChecked = true
            }
        }
    }
}
