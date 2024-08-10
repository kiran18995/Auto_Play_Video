package com.kiran.autoplayvideo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.kiran.autoplayvideo.customview.adapter.VideosAdapter
import com.kiran.autoplayvideo.databinding.ActivityMainBinding
import com.kiran.autoplayvideo.models.VideoResponse
import com.kiran.autoplayvideo.util.VerticalSpacingItemDecorator
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        val itemDecorator = VerticalSpacingItemDecorator(10)
        binding.recyclerView.addItemDecoration(itemDecorator)

        val jsonString = readJsonFromAssets()
        val gson = Gson()
        val videoResponse = gson.fromJson(jsonString, VideoResponse::class.java)
        val videos = videoResponse.videos
        binding.recyclerView.setMediaObjects(videos)
        val adapter = VideosAdapter(videos, initGlide())
        binding.recyclerView.adapter = adapter
    }

    private fun readJsonFromAssets(fileName: String = "media.json"): String? {
        return try {
            assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            null
        }
    }

    private fun initGlide(): RequestManager {
        val options = RequestOptions().placeholder(R.drawable.white_background)
            .error(R.drawable.white_background)

        return Glide.with(this).setDefaultRequestOptions(options)
    }

    override fun onDestroy() {
        binding.recyclerView.releasePlayer()
        super.onDestroy()
    }
}