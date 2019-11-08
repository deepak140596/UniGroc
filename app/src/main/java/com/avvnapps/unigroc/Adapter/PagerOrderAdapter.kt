package com.avvnapps.unigroc.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.avvnapps.unigroc.Fragments.PastOrder
import com.avvnapps.unigroc.Fragments.Pending

class PagerOrderAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        when (position) {
             0 -> return Pending()
             1 -> return PastOrder()

        }

        return Fragment()
    }

    override fun getCount(): Int {
        return 2
    }
    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Pending"
            1 -> "Past Order"
            else -> "Pending"
        }
    }
}