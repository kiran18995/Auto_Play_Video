package com.kiran.autoplayvideo.customview.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.kiran.autoplayvideo.R
import com.kiran.autoplayvideo.models.Video


class VideoPlayerViewHolder(private var parent: View) : RecyclerView.ViewHolder(parent) {
    var thumbnailUrlnail: ImageView = itemView.findViewById(R.id.thumbnailUrlnail)
    var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    var requestManager: RequestManager? = null

    fun onBind(mediaObject: Video, requestManager: RequestManager?) {
        this.requestManager = requestManager
        parent.tag = this
        this.requestManager?.load(mediaObject.thumbnailUrl)?.centerCrop()?.into(thumbnailUrlnail)
    }
}