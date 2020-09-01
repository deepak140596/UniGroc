package com.avvnapps.unigroc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.avvnapps.unigroc.database.firestore.FirestoreRepository
import com.avvnapps.unigroc.models.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class FirestoreViewModel(application: Application) : AndroidViewModel(application) {

    val TAG by lazy { "FIRESTORE_VIEW_MODEL" }
    private var firebaseRepository = FirestoreRepository()
    var availableCartItems: MutableLiveData<List<CartEntity>> = MutableLiveData()
    var savedAddresses: MutableLiveData<List<AddressItem>> = MutableLiveData()
    var quotedPrices: MutableLiveData<List<RetailerQuotationItem>> = MutableLiveData()
    var allOrdersList: MutableLiveData<List<OrderItem>> = MutableLiveData()
    var wishListItems: MutableLiveData<List<wishlistItems>> = MutableLiveData()

    //Orders
    private var quotedOrdersList: MutableLiveData<List<OrderItem>> = MutableLiveData()
    var orderHistoryList: MutableLiveData<List<OrderItem>> = MutableLiveData()

    private var retailerReviews: MutableLiveData<List<Review>> = MutableLiveData()

    private var sliderList: MutableLiveData<List<SliderModel>> = MutableLiveData()


    // get realtime updates from firebase regarding slider
    fun getSlider(): LiveData<List<SliderModel>> {

        sliderList = MutableLiveData()
        firebaseRepository.getBannerData().addOnSuccessListener { documents ->
            val availableSlider: MutableList<SliderModel> = mutableListOf()

            for (document in documents) {
                val noOfBanner: Long = document.get("no_of_banners") as Long
                for (n in 1..noOfBanner) {
                    availableSlider.add(
                        SliderModel(
                            document.get("banner_$n").toString(),
                            document.get("banner_${n}_background").toString()
                        )
                    )
                }

            }
            sliderList.value = availableSlider

        }.addOnFailureListener { exception ->
            sliderList.value = null
            Log.w(TAG, "Error getting Banner: ", exception)
        }
        return sliderList

    }


    // get available cart items from firestore
    fun getAvailableCartItems(): LiveData<List<CartEntity>> {

        availableCartItems = MutableLiveData()
        firebaseRepository.getAvailableCartItems().addOnSuccessListener { documents ->
            val availableCartList: MutableList<CartEntity> = mutableListOf()
            for (doc in documents) {
                val cartItem = doc.toObject(CartEntity::class.java)
                availableCartList.add(cartItem)
            }

            availableCartItems.value = availableCartList

        }.addOnFailureListener {
            availableCartItems.value = null
        }

        return availableCartItems

    }

    // get wishlist items from firestore
    fun getWishListItem(): LiveData<List<wishlistItems>> {
        wishListItems = MutableLiveData()
        firebaseRepository.getWishlistItems().addOnSuccessListener { document ->
            val wishlistItemsList: MutableList<wishlistItems> = mutableListOf()
            for (doc in document) {
                val wishlistItem = doc.toObject(wishlistItems::class.java)
                wishlistItemsList.add(wishlistItem)
            }
            wishListItems.value = wishlistItemsList
        }.addOnFailureListener {
            wishListItems.value = null

        }
        return wishListItems
    }

    // save address to firebase
    fun saveAddressToFirebase(addressItem: AddressItem) {
        firebaseRepository.saveAddressItem(addressItem).addOnFailureListener {
            Log.e(TAG, "Failed to save Address!")
        }
    }

    // get realtime updates from firebase regarding saved addresses
    fun getSavedAddresses(): LiveData<List<AddressItem>> {
        firebaseRepository.getSavedAddress()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    savedAddresses.value = null
                    return@EventListener
                }

                val savedAddressList: MutableList<AddressItem> = mutableListOf()
                for (doc in value!!) {
                    val addressItem = doc.toObject(AddressItem::class.java)
                    savedAddressList.add(addressItem)
                }
                savedAddresses.value = savedAddressList
            })

        return savedAddresses
    }

    // delete an address from firebase
    fun deleteAddress(addressItem: AddressItem) {
        firebaseRepository.deleteAddress(addressItem).addOnFailureListener {
            Log.e(TAG, "Failed to delete Address")
        }
    }

    // delete an order Item from firebase
    fun cancelOrder(orderItem: OrderItem) {
        firebaseRepository.deleteOrderItem(orderItem).addOnFailureListener {
            Log.e(TAG, "Failed to delete Address")
        }
    }

    fun submitOrder(orderItem: OrderItem, cartViewModel: CartViewModel) {
        firebaseRepository.submitOrder(orderItem)
            .addOnSuccessListener {
                // when order is successfully submitted, clear the cart
                cartViewModel.deleteAll()
                Log.i(TAG, "Order Placed!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to submit Order: $e")
            }
    }

    fun getQuotedPrices(orderItem: OrderItem): MutableLiveData<List<RetailerQuotationItem>> {
        firebaseRepository.getQuotedPrices(orderItem)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w(TAG, "Get Quoted Price failed", e)
                    quotedPrices.value = null
                    return@EventListener
                }

                val qPrices: MutableList<RetailerQuotationItem> = mutableListOf()
                for (doc in value!!) {
                    val quotationItem = doc.toObject(RetailerQuotationItem::class.java)
                    qPrices.add(quotationItem)
                }
                quotedPrices.value = qPrices

            })
        return quotedPrices
    }

    fun getQuotedOrders(): MutableLiveData<List<OrderItem>> {

        firebaseRepository.getQuotedOrders().addSnapshotListener { value, e ->
            if (e != null) {
                Log.e(TAG, "Failed to retrieve quoted prices, $e")
                quotedOrdersList.value = null
                return@addSnapshotListener
            }
            val qOrders: MutableList<OrderItem> = mutableListOf()
            for (doc in value!!) {
                val orderItem = doc.toObject(OrderItem::class.java)
                qOrders.add(orderItem)
            }
            quotedOrdersList.value = qOrders
        }
        return quotedOrdersList
    }

    fun getOrdersHistory(): MutableLiveData<List<OrderItem>> {
        firebaseRepository.getOrderHistory().addOnSuccessListener {
            val qOrders: MutableList<OrderItem> = mutableListOf()
            for (doc in it) {
                val orderItem = doc.toObject(OrderItem::class.java)
                qOrders.add(orderItem)
            }
            orderHistoryList.value = qOrders
        }.addOnFailureListener {
            Log.e(TAG, "Failed to retrieve quoted prices", it)
            orderHistoryList.value = null
        }

        return orderHistoryList
    }

    fun getAllOrders(): MutableLiveData<List<OrderItem>> {
        firebaseRepository.getAllOrders()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    allOrdersList.value = null
                    return@EventListener
                }

                var ordersList: MutableList<OrderItem> = mutableListOf()
                for (doc in value!!) {
                    var orderItem = doc.toObject(OrderItem::class.java)
                    ordersList.add(orderItem)
                }
                allOrdersList.value = ordersList
            })

        return allOrdersList
    }

    fun placeOrder(orderItem: OrderItem, retailerId: String, cartItems: List<CartEntity>) {
        firebaseRepository.placeOrder(orderItem, retailerId, cartItems).addOnFailureListener {
            Log.e(TAG, "Order Placing Failed! : $it")
        }
    }

    // get realtime updates from firebase regarding reviews
    fun getReviews(retailerId: String): LiveData<List<Review>> {
        firebaseRepository.getReviews(retailerId)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    savedAddresses.value = null
                    return@EventListener
                }

                val retailerReviewsList: MutableList<Review> = mutableListOf()
                for (doc in value!!) {
                    val addressItem = doc.toObject(Review::class.java)
                    retailerReviewsList.add(addressItem)
                }
                retailerReviews.value = retailerReviewsList
            })

        return retailerReviews
    }

}