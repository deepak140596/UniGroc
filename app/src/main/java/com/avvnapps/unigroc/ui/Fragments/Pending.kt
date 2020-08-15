package com.avvnapps.unigroc.ui.Fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.ui.order_status.OrderItemAdapter
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.fragment_pending.*


class Pending : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    val TAG = "PENDING_ORDERS"

    lateinit var activity: AppCompatActivity

    var submittedOrders: List<OrderItem> = emptyList()
    private val firestoreViewModel by lazy {
        ViewModelProvider(this).get(FirestoreViewModel::class.java)
    }

    lateinit var adapter: OrderItemAdapter

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
        // initialise Firestore VM
        activity_pending_progress_bar.visibility = View.VISIBLE
        initialiseFirestoreViewModel()

        fragment_pending_recycler_view.layoutManager = LinearLayoutManager(activity)

        adapter = OrderItemAdapter(
            activity,
            submittedOrders,
            firestoreViewModel
        ) { orderItem -> placeOrder(orderItem) }

        fragment_pending_recycler_view.adapter = adapter

        fragment_pending_swipe_layout.setOnRefreshListener(this)
    }

    private fun placeOrder(orderItem: OrderItem) {
        val quotations = orderItem.quotations

        if (quotations.size > 0) {
            firestoreViewModel.placeOrder(
                orderItem,
                quotations[0].retailerId,
                quotations[0].cartItems
            )
        }

        if (quotations.size > 1) {
            firestoreViewModel.placeOrder(
                orderItem,
                quotations[1].retailerId,
                quotations[1].cartItems
            )
        }

        if (quotations.size > 2) {
            firestoreViewModel.placeOrder(
                orderItem,
                quotations[2].retailerId,
                quotations[2].cartItems
            )
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

    override fun onRefresh() {
        initialiseFirestoreViewModel()
    }

    override fun onResume() {
        super.onResume()
        initialiseFirestoreViewModel()
    }
}
