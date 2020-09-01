package com.avvnapps.unigroc.ui.home_page

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avvnapps.unigroc.R
import com.avvnapps.unigroc.models.SliderModel
import com.avvnapps.unigroc.utils.GlideApp
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.islamkhsh.CardSliderAdapter
import kotlinx.android.synthetic.main.banner_item.view.*

class BannerViewAdapter(
    var context: Context,
    var data: List<SliderModel>
) :
    CardSliderAdapter<BannerViewAdapter.HomeBannersVH>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBannersVH {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.banner_item,
            parent, false
        )
        return HomeBannersVH(view)
    }

    override fun getItemCount(): Int = data.size

    inner class HomeBannersVH(view: View) :
        RecyclerView.ViewHolder(view) {

        fun onBound(
            context: Context,
            SlideBanner: SliderModel
        ) {
            val options: RequestOptions = RequestOptions()
                .placeholder(R.drawable.slider_placeholder)
                .error(R.drawable.slider_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)

            GlideApp.with(context)
                .applyDefaultRequestOptions(options)
                .load(SlideBanner.banner)
                .into(itemView.BannerImageView)
        }

    }

    override fun bindVH(holder: HomeBannersVH, position: Int) {
        holder.onBound(context, data[position])
    }
}