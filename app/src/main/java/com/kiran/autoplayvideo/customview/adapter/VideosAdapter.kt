package com.kiran.autoplayvideo.customview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.kiran.autoplayvideo.R
import com.kiran.autoplayvideo.models.Video

class VideosAdapter(
    private val mediaObjects: List<Video>, private val requestManager: RequestManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_video_list_item, parent, false)
        return VideoPlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as VideoPlayerViewHolder).onBind(mediaObjects[position], requestManager)
    }

    override fun getItemCount(): Int = mediaObjects.size
}