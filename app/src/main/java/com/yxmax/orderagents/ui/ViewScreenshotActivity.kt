package com.yxmax.orderagents.ui

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yxmax.orderagents.BaseActivity
import com.yxmax.orderagents.utils.sendToast


class ViewScreenshotActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageView = ImageView(this)
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER)
        setContentView(imageView)

        val intent = getIntent()
        val imageUri = intent.getData()

        Log.i("OrderAgents","查看截图: " + imageUri)

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .skipMemoryCache(true) // 测试时跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 测试时跳过磁盘缓存
                .into(imageView)
        } else {
            sendToast("暂无图片")
            finish()
        }
    }
}