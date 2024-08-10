package com.kiran.autoplayvideo.customview

import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.kiran.autoplayvideo.R
import com.kiran.autoplayvideo.customview.adapter.VideoPlayerViewHolder
import com.kiran.autoplayvideo.models.Video

@Suppress("DEPRECATION")
class VideoPlayerRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private enum class VolumeState { ON, OFF }

    private companion object{
        private const val TAG = "VideoPlayerRecyclerView"
    }

    // UI
    private var thumbnailUrl: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var viewHolderParent: View? = null
    private var frameLayout: FrameLayout? = null
    private var videoSurfaceView: PlayerView? = null
    private var videoPlayer: ExoPlayer? = null

    // Vars
    private var mediaObjects: List<Video> = emptyList()
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var isVideoViewAdded = false
    private var requestManager: RequestManager? = null
    private var volumeState: VolumeState = VolumeState.ON

    init {
        init(context)
    }

    @OptIn(UnstableApi::class)
    private fun init(context: Context) {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display: Display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val point = Point()
        display.getRealSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y

        videoSurfaceView = PlayerView(context).apply {
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }

        videoPlayer =
            ExoPlayer.Builder(context).setMediaSourceFactory(DefaultMediaSourceFactory(context))
                .build().apply {
                    videoSurfaceView?.player = this
                    playWhenReady = true
                }

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.")
                    thumbnailUrl?.visibility = VISIBLE

                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true)
                    } else {
                        playVideo(false)
                    }
                }
            }
        })

        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}

            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent == view) {
                    resetVideoView()
                }
            }
        })

        videoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.e(TAG, "onPlayerStateChanged: Buffering video.")
                        progressBar?.visibility = VISIBLE
                    }

                    Player.STATE_READY -> {
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.")
                        progressBar?.visibility = GONE
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }

                    Player.STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged: Video ended.")
                        videoPlayer?.seekTo(0)
                    }

                    Player.STATE_IDLE -> {}
                }
            }
        })
    }

    fun playVideo(isEndOfList: Boolean) {
        val targetPosition: Int

        if (!isEndOfList) {
            val startPosition =
                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val endPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            targetPosition = if (endPosition - startPosition > 1) {
                val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)
                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }

            if (startPosition < 0 || endPosition < 0) return
        } else {
            targetPosition = mediaObjects.size - 1
        }

        Log.d(TAG, "playVideo: target position: $targetPosition")

        if (targetPosition == playPosition) return

        playPosition = targetPosition
        if (videoSurfaceView == null) return

        videoSurfaceView?.visibility = INVISIBLE
        removeVideoView(videoSurfaceView!!)

        val currentPosition =
            targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return

        val holder = child.tag as? VideoPlayerViewHolder ?: run {
            playPosition = -1
            return
        }
        thumbnailUrl = holder.thumbnailUrlnail
        progressBar = holder.progressBar
        viewHolderParent = holder.itemView
        requestManager = holder.requestManager
        frameLayout = holder.itemView.findViewById(R.id.media_container)

        videoSurfaceView?.player = videoPlayer

        val mediaUrl = mediaObjects[targetPosition].mediaUrl
        val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))
        videoPlayer?.setMediaItem(mediaItem)
        videoPlayer?.prepare()
        videoPlayer?.playWhenReady = true
    }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: $at")

        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)

        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    private fun removeVideoView(videoView: PlayerView) {
        val parent = videoView.parent as? ViewGroup ?: return
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
            viewHolderParent?.setOnClickListener(null)
        }
    }

    private fun addVideoView() {
        frameLayout?.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView?.requestFocus()
        videoSurfaceView?.visibility = VISIBLE
        videoSurfaceView?.alpha = 1f
        thumbnailUrl?.visibility = GONE
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView!!)
            playPosition = -1
            videoSurfaceView?.visibility = INVISIBLE
            thumbnailUrl?.visibility = VISIBLE
        }
    }

    fun releasePlayer() {
        videoPlayer?.release()
        videoPlayer = null
        viewHolderParent = null
    }

    private fun toggleVolume() {
        if (videoPlayer != null) {
            volumeState = if (volumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.")
                VolumeState.ON
            } else {
                Log.d(TAG, "togglePlaybackState: disabling volume.")
                VolumeState.OFF
            }
        }
    }

    fun setMediaObjects(mediaObjects: List<Video>) {
        this.mediaObjects = mediaObjects
        if (mediaObjects.isNotEmpty()) {
            // Play the first item when the data is set
            playVideo(false)
        }
    }
}