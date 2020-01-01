package com.avvnapps.unigroc.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.OrderItem
import com.avvnapps.unigroc.order_status.OrderItemAdapter
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import kotlinx.android.synthetic.main.fragment_past_order.*


class PastOrder : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    val TAG = "PAST_ORDERS"

    lateinit var activity: AppCompatActivity

    var ordersHistory: List<OrderItem> = emptyList()
    lateinit var firestoreViewModel: FirestoreViewModel
    lateinit var adapter: OrderItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity = getActivity() as AppCompatActivity
        return inflater.inflate(R.layout.fragment_past_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity_past_order_progress_bar.visibility = View.VISIBLE

        // initialise Firestore VM
        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
        initialiseFirestoreViewModel()

        fragment_past_order_recycler_view.layoutManager = LinearLayoutManager(activity)

        adapter = OrderItemAdapter(activity, ordersHistory,firestoreViewModel)
        fragment_past_order_recycler_view.adapter = adapter

        fragment_past_order_swipe_layout.setOnRefreshListener(this)

    }

    private fun initialiseFirestoreViewModel() {
        firestoreViewModel.getOrdersHistory().observe(this, Observer { orders ->
            Log.i(TAG, "OrdersSize: ${orders.size}")

            ordersHistory = orders
            if (ordersHistory.isNullOrEmpty()){
                emptyShoppingBag.visibility = View.VISIBLE
                fragment_past_order_recycler_view.visibility = View.GONE
            }else{
                emptyShoppingBag.visibility = View.GONE
                fragment_past_order_recycler_view.visibility = View.VISIBLE
            }
            adapter.orderList = ordersHistory
            adapter.notifyDataSetChanged()



            if (fragment_past_order_swipe_layout.isRefreshing)
                fragment_past_order_swipe_layout.isRefreshing = false

            activity_past_order_progress_bar.visibility = View.GONE

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
