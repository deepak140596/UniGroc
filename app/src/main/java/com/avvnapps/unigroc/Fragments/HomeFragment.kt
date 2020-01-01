package com.avvnapps.unigroc.Fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.generate_cart.SearchItemActivity


import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import kotlinx.android.synthetic.main.fragment_home.*


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
        inflateSLider()

        return viewOfLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun inflateSLider() {
        val imageSlider = viewOfLayout.findViewById<ImageSlider>(R.id.image_slider)

        val listURL = ArrayList<SlideModel>()
        listURL.add(SlideModel("https://i.imgur.com/F142xAr.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/rjUxZOJ.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/B3vqL4T.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/DeRFbNz.jpg"))
        listURL.add(SlideModel("https://i.imgur.com/tuQ03J9.jpg"))


        imageSlider.setImageList(listURL,true)

    }


}
