package com.avvnapps.unigroc.ui.Fragments


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.database.network.ICloudFunctions
import com.avvnapps.unigroc.database.network.RetrofitCloudClient
import com.avvnapps.unigroc.models.BraintreeTransaction
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.ui.order_status.OrderItemAdapter
import com.avvnapps.unigroc.utils.successToast
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_pending.*


class Pending : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    OrderItemAdapter.onItemClickListener {

    val TAG = "PENDING_ORDERS"

    lateinit var activity: AppCompatActivity
    private var submittedOrders: List<OrderItem> = emptyList()
    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }
    lateinit var adapter: OrderItemAdapter

    private val REQUEST_CODE: Int = 1234
    private var token: String? = null
    private var compositeDisposable = CompositeDisposable()
    private val myAPI by lazy { RetrofitCloudClient.instance.create(ICloudFunctions::class.java) }
    private val subTotalPrice: Double? = 0.0

    private var orderItemData: OrderItem? = null
    private var retailerCode = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        activity = getActivity() as AppCompatActivity
        return inflater.inflate(R.layout.fragment_pending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the Token
        compositeDisposable.add(
            myAPI.getToken()!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { braintreeToken ->
                        token = braintreeToken!!.token
                        Log.d(TAG, token)

                    },
                    { throwable ->
                        //Toast.makeText(activity, "" + throwable.message, Toast.LENGTH_SHORT).show()
                    })
        )
        // initialise Firestore VM
        activity_pending_progress_bar.visibility = View.VISIBLE
        initialiseFirestoreViewModel()

        fragment_pending_recycler_view.layoutManager = LinearLayoutManager(activity)

        adapter = OrderItemAdapter(
            activity,
            submittedOrders,
            firestoreViewModel,
            this
        )

        fragment_pending_recycler_view.adapter = adapter

        fragment_pending_swipe_layout.setOnRefreshListener(this)
    }


    private fun submitPayment() {
        val dropInRequest = DropInRequest().clientToken(token)
        startActivityForResult(dropInRequest.getIntent(activity), REQUEST_CODE)
    }

    private fun showPayDialogBox(orderItem: OrderItem, retailerCode: Int) {

        val quotations = orderItem.quotations
        var retailerName = ""

        if (retailerCode == 1) {
            retailerName = quotations[0].retailerName
        }

        if (retailerCode == 2) {
            retailerName = quotations[1].retailerName
        }

        if (retailerCode == 3) {
            retailerName = quotations[2].retailerName
        }


        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Place Order")
            .setMessage("Go ahead and Place order to $retailerName ?")
            .setPositiveButton("Yes") { dialog, which ->
                submitPayment()
                dialog.dismiss()
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()

            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun placeOrder() {
        val quotations = orderItemData?.quotations
        if (retailerCode == 1) {
            orderItemData?.let {
                firestoreViewModel.placeOrder(
                    it,
                    quotations?.get(0)?.retailerId.toString(),
                    quotations?.get(0)?.cartItems!!
                )
                activity.successToast("Order has placed")

            }
        }

        if (retailerCode == 2) {
            orderItemData?.let {
                firestoreViewModel.placeOrder(
                    it,
                    quotations?.get(1)?.retailerId.toString(),
                    quotations?.get(1)?.cartItems!!
                )
                activity.successToast("Order has placed")

            }
        }

        if (retailerCode == 3) {
            orderItemData?.let {
                firestoreViewModel.placeOrder(
                    it,
                    quotations?.get(2)?.retailerId.toString(),
                    quotations?.get(2)?.cartItems!!
                )
                activity.successToast("Order has placed")
            }
        }
    }

    private fun initialiseFirestoreViewModel() {
        firestoreViewModel.getQuotedOrders().observe(viewLifecycleOwner, Observer { orders ->
            Log.i(TAG, "OrdersSize: ${orders.size}")
            submittedOrders = orders
            if (submittedOrders.isNullOrEmpty()) {
                empty_cart.visibility = View.VISIBLE
                fragment_pending_recycler_view.visibility = View.GONE
            } else {
                empty_cart.visibility = View.GONE
                fragment_pending_recycler_view.visibility = View.VISIBLE
            }
            adapter.orderList = submittedOrders
            adapter.notifyDataSetChanged()

            if (fragment_pending_swipe_layout.isRefreshing)
                fragment_pending_swipe_layout.isRefreshing = false

            activity_pending_progress_bar.visibility = View.GONE

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result =
                    data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce = result.paymentMethodNonce!!.nonce

                subTotalPrice?.let {
                    compositeDisposable.add(
                        myAPI.submitPayment(
                            it,
                            nonce
                        )?.subscribeOn(Schedulers.io())
                            ?.observeOn(AndroidSchedulers.mainThread())
                            ?.subscribe({ t: BraintreeTransaction? ->
                                if (t!!.success) {
                                    placeOrder()
                                    /* Toast.makeText(
                                     this,
                                     "" + t.transaction!!.id,
                                     Toast.LENGTH_SHORT
                                 ).show()*/

                                    Log.d(TAG, t.transaction!!.toString())
                                }
                            },
                                { t: Throwable? ->
                                    Toast.makeText(
                                        activity,
                                        "" + t!!.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })!!
                    )
                }


            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onRefresh() {
        initialiseFirestoreViewModel()
    }

    override fun onResume() {
        super.onResume()
        initialiseFirestoreViewModel()
    }

    override fun retailerOnePlaceOrder(orderItem: OrderItem) {
        orderItemData = orderItem
        retailerCode = 1
        showPayDialogBox(orderItem, 1)
    }

    override fun retailerTwoPlaceOrder(orderItem: OrderItem) {
        orderItemData = orderItem
        retailerCode = 2
        showPayDialogBox(orderItem, 2)
    }

    override fun retailerThreePlaceOrder(orderItem: OrderItem) {
        orderItemData = orderItem
        retailerCode = 3
        showPayDialogBox(orderItem, 3)
    }

}
