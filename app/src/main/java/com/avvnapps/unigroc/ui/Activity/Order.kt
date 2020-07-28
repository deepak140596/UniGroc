package com.avvnapps.unigroc.ui.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.avvnapps.unigroc.Adapter.PagerOrderAdapter
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.ui.MainActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_order.*

class Order : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Order"
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        })


        val tabLayout = findViewById<TabLayout>(R.id.order_tab_layout)

        tabLayout.addTab(tabLayout.newTab().setText("Pending"))
        tabLayout.addTab(tabLayout.newTab().setText("Past Order"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        order_pager.adapter =
            PagerOrderAdapter(supportFragmentManager)
        order_pager.offscreenPageLimit = 0
        order_tab_layout.setupWithViewPager(order_pager)
        val adapter = PagerOrderAdapter(supportFragmentManager)
        order_pager.adapter = adapter
        wrapTabIndicatorToTitle(tabLayout, 80, 80)
        order_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                order_pager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

    }
    fun wrapTabIndicatorToTitle(tabLayout: TabLayout, externalMargin: Int, internalMargin: Int) {
        val tabStrip = tabLayout.getChildAt(0)
        if (tabStrip is ViewGroup) {
            val childCount = tabStrip.childCount
            for (i in 0 until childCount) {
                val tabView = tabStrip.getChildAt(i)
                tabView.minimumWidth = 0
                tabView.setPadding(0, tabView.paddingTop, 0, tabView.paddingBottom)
                if (tabView.layoutParams is ViewGroup.MarginLayoutParams) {
                    val layoutParams = tabView.layoutParams as ViewGroup.MarginLayoutParams
                    if (i == 0) {
                        setMargin(layoutParams, externalMargin, internalMargin)
                    } else if (i == childCount - 1) {
                        setMargin(layoutParams, internalMargin, externalMargin)
                    } else {
                        setMargin(layoutParams, internalMargin, internalMargin)
                    }
                }
            }

            tabLayout.requestLayout()
        }
    }

    private fun setMargin(layoutParams: ViewGroup.MarginLayoutParams, start: Int, end: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.marginStart = start
            layoutParams.marginEnd = end
        } else {
            layoutParams.leftMargin = start
            layoutParams.rightMargin = end
        }
    }
}
