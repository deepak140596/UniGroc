package com.avvnapps.unigroc

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.avvnapps.unigroc.Font.CustomTypefaceSpan
import com.avvnapps.unigroc.Fragments.HomeFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var navigationView: NavigationView? = null
    internal var padding = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startFragment(HomeFragment())

        //inflate Navigation Drawer
        inflateNavDrawer();

    }

    fun startFragment(fragment : Fragment){
        if(fragment != null){
            var fragmentManager =supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.contentPanel,fragment,"").commit()
        }
    }

    private fun inflateNavDrawer(){
        //set Custom toolbar to activity -----------------------------------------------------------
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setPadding(padding, toolbar!!.getPaddingTop(), padding, toolbar!!.getPaddingBottom())
        setSupportActionBar(toolbar)
        for (i in 0 until toolbar!!.getChildCount()) {
            val view = toolbar!!.getChildAt(i)

            if (view is TextView) {

                val myCustomFont = Typeface.createFromAsset(assets, "Font/Bold.otf")
                view.typeface = myCustomFont
            }

        }
        supportActionBar!!.setTitle(resources.getString(R.string.app_name))

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        navigationView = findViewById(R.id.nav_view) as NavigationView
        val m = navigationView!!.getMenu()
        for (i in 0 until m.size()) {
            val mi = m.getItem(i)

            //for aapplying a font to subMenu ...
            val subMenu = mi.getSubMenu()
            if (subMenu != null && subMenu!!.size() > 0) {
                for (j in 0 until subMenu!!.size()) {
                    val subMenuItem = subMenu!!.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi)
        }

    }

    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "Font/Bold.otf")
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(
            CustomTypefaceSpan("", font),
            0,
            mNewTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        mi.title = mNewTitle
    }

}
