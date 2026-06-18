package com.yxmax.orderagents.utils.processor

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.`object`.OrderRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScreenshotProcessor {

    fun saveScreenshotToCache(bitmap: Bitmap, order: String): Uri? {
        try {
            val context = GlobalApplication.Companion.context
            // 1. 获取缓存目录
            val cachePath = File(context.getCacheDir(), "images")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }

            Log.i("OrderAgents","缓存的图片数量: " + cachePath.listFiles().size.toString())

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "img_${timeStamp}.png"

            // 2. 创建文件
            val file = File(cachePath, fileName)
            if(file.exists()){
                file.delete()
            }
            val stream = FileOutputStream(file)


            // 3. 写入数据
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            Log.i("OrderAgents","记录的文件名: " + file.name)

            // 4. 获取Uri (使用 FileProvider 以适配 Android 7.0+)
            return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun removeScreenshotFromCache(uri: Uri) {
        val file_name = this.getFileNameFromUri(uri)
        try {
            val context = GlobalApplication.Companion.context
            // 1. 重建文件路径 (必须与保存时的路径逻辑一致)
            val cachePath = File(context.getCacheDir(), "images")
            val file = File(cachePath, file_name)

            // 2. 检查文件是否存在并删除
            if (file.exists()) {
                if (file.delete()) {
                    Log.i("OrderAgents","截图缓存删除成功: " + file_name)
                } else {
                    Log.i("OrderAgents","截图缓存删除失败: " + file_name)
                }
            }

            if(OrderRepository.canCleanCache()){
                val parentDir = file.parentFile
                if (parentDir != null && parentDir.exists()) {
                    if(parentDir.listFiles().isNullOrEmpty()){
                        return
                    }
                    parentDir.walkBottomUp() // 自下而上遍历，确保先删子文件再删空文件夹
                        .filter { it.isFile } // 只挑选文件进行删除
                        .forEach { it.delete() }
                    Log.i("OrderAgents","多余截图缓存删除成功")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val context = GlobalApplication.Companion.context
        // 通过 ContentResolver 查询 Uri 的元数据
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                // OpenableColumns.DISPLAY_NAME 包含了文件名和后缀（如 "20260518_224753.png"）
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }

        return fileName
    }
}