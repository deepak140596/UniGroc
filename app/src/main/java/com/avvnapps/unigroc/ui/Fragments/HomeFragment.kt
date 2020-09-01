package com.avvnapps.unigroc.ui.Fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.avvnapps.unigroc.R


class HomeFragment : Fragment() {

    private lateinit var viewOfLayout: View
    lateinit var activity: AppCompatActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity = getActivity() as AppCompatActivity
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_home, container, false)

        return viewOfLayout
    }




}
