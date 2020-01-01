package com.avvnapps.unigroc.location_address

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.SharedPreferencesDB
import com.avvnapps.unigroc.models.AddressItem
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_address.*
import java.lang.reflect.Array
import java.util.*


class CreateAddressActivity : AppCompatActivity() {
    val TAG = "CREATE_ADDRESS"
    val PLACE_PICKER_REQUEST = 1
    var latitutde : Double ?= null
    var longitude : Double ?= null
    var firestoreViewModel : FirestoreViewModel ?=null
    var addressItem: AddressItem?=null

    lateinit var fields:List<Place.Field>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_address)


        var data = intent.getSerializableExtra("address_item")
        if(data != null )
            addressItem = data as AddressItem
        if(addressItem != null){
            setupViews()
        }

        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)

        create_address_set_location_btn.setOnClickListener {
            createPlacePicker()
        }

        create_address_done_fab.setOnClickListener {
            if(isFormValid()){
                var addressItem = AddressItem(
                    create_address_name_edit_text.text.toString(),
                    create_address_name_edit_text.text.toString(),
                    create_address_house_name_edit_text.text.toString(),
                    create_address_locality_edit_text.text.toString(),
                    create_address_landmark_edit_text.text.toString(),
                    latitutde!!, longitude!!
                )

                firestoreViewModel!!.saveAddressToFirebase(addressItem)
                // save the selected address as default
                SharedPreferencesDB.savePreferredAddress(this@CreateAddressActivity,addressItem)
                finish()
            }
        }

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyCugE5Tb5kWS5FfOyxHT0LuF3aEJoi6rKA");
        // Create a new Places client instance.
        val placesClient = Places.createClient(this)

        fields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG)

    }

    fun setupViews(){
        create_address_house_name_edit_text.setText(addressItem!!.houseName)
        create_address_locality_edit_text.setText(addressItem!!.locality)
        create_address_landmark_edit_text.setText( addressItem!!.landmark)
        create_address_name_edit_text.setText( addressItem!!.addressName)
        latitutde = addressItem!!.latitude
        longitude = addressItem!!.longitude
        create_address_is_location_set_cb.isChecked = true
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

    /*    val builder = PlacePicker.IntentBuilder()
        try {
            Log.d(TAG, "opening startActivityforResult")
            startActivityForResult(builder.build(this@CreateAddressActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }*/
        var intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
            .build(this)
        startActivityForResult(intent,PLACE_PICKER_REQUEST)

    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {

                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                val latLng = place?.latLng
                Log.d(TAG, "LatLng: $latLng")
                latitutde = latLng?.latitude
                longitude = latLng?.longitude

                create_address_is_location_set_cb.isChecked = true
            }
        }
    }
}
