package com.kiran.autoplayvideo.models

data class Video(
    val mediaUrl: String,
    val thumbnailUrl: String
)

data class VideoResponse(
    val videos: List<Video>
)