package com.yxmax.orderagents.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.GlobalApplication.Companion.notificationContent
import com.yxmax.orderagents.MainActivity
import com.yxmax.orderagents.R
import com.yxmax.orderagents.`object`.OrderInfo
import com.yxmax.orderagents.`object`.OrderRepository
import com.yxmax.orderagents.receive.CheckButtonReceiver
import com.yxmax.orderagents.ui.ViewScreenshotActivity
import com.yxmax.orderagents.utils.processor.RecognizeProcessor
import com.yxmax.orderagents.utils.sendToast

class LiveNotificationManager() {

    val context = GlobalApplication.context

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "orderagents_live"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "物品码通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "实况通知频道"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun testLiveNotificationDemo(){
        createNotification(null,"mcdonald","35301","com.yxmax.orderagents",1)
        sendToast("已发送可点击实况通知")
    }

    fun createNotification(uri: Uri?, image: String, order: String?, pack: String,type: Int){
        if(order == null) return
        if(OrderRepository.hasOrder(order)) return
        if(image == "default"){
            Log.i("OrderAgents","未在OCR文字中获取到具体品牌")
        } else {
            Log.i("OrderAgents","成功在OCR文字中获取品牌: " + image)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA){
            if(Build.MANUFACTURER.equals("meizu")){
                this.showFlymeLiveNotification(uri,image,order,pack,type)
            } else {
                this.showAndroidLiveUpdates(uri,image,order,pack,type)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun showAndroidLiveUpdates(uri: Uri?, image: String, order: String, pack: String,type: Int){

        val factoryInfo = RecognizeProcessor.getFactory(image,pack)
        val title = factoryInfo.translate
        val img = factoryInfo.image
        val id = OrderRepository.getNextId()
        OrderRepository.addCardItem(
            OrderInfo(
                uri,
                img,
                title,
                order,
                id,
                type
            )
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.shopping_cart)
            .setLargeIcon(img)
            .setContentTitle(title)
            .setContentText(order)
            .setContentIntent(this.getOpenedIntent(pack))
            .setOngoing(true)
            .setShortCriticalText(order)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setRequestPromotedOngoing(true)

        if(type >= 2){
            builder.addAction(0,"标记完成",this.getCompletedIntent(id))
        } else {
            builder.addAction(0,"查看截图",this.getScreenshotIntent(uri))
                .addAction(0,"标记完成",this.getCompletedIntent(id))
        }

        notificationManager.notify(id,builder.build())
    }

    fun showFlymeLiveNotification(uri: Uri?, image: String, order: String, pack: String,contentType: Int) {

        val id = OrderRepository.getNextId()
        val factoryInfo = RecognizeProcessor.getFactory(image,pack)
        val title = factoryInfo.translate
        val img = factoryInfo.image
        val color = factoryInfo.color
        if(id == 1006) return
        var type = contentType
        if(image.equals("mcdonald") && order.contains("取餐柜")){
            type = -1
        }
        OrderRepository.addCardItem(
            OrderInfo(
                uri,
                img,
                title,
                order,
                id,
                type
            )
        )

        // 创建胶囊内容
        val capsuleRemoteViews = RemoteViews(context.packageName, R.layout.live_notification_capsule)
        capsuleRemoteViews.setTextViewText(R.id.capsule_content, order)

        // 创建胶囊配置
        val capsuleBundle = Bundle().apply {
            putInt("notification.live.capsuleStatus", 1)
            putInt("notification.live.capsuleType", 5)
            putString("notification.live.capsuleContent", resetCapsuleCodeDisplay(order,type))
            putParcelable("notification.live.capsuleIcon", Icon.createWithBitmap(img))

            putInt("notification.live.capsuleBgColor",color.toArgb())
            putInt("notification.live.capsuleContentColor", context.resources.getColor(color.getIslandTextColor(), null))
            putParcelable("notification.live.capsule.content.remote.view", capsuleRemoteViews)
        }

        // 创建实况通知Bundle
        val liveBundle = Bundle().apply {
            putBoolean("is_live", true)
            putInt("notification.live.operation", 0)
            putInt("notification.live.type",2)
            putBundle("notification.live.capsule", capsuleBundle)
        }

        // 创建主通知内容
        val contentRemoteViews = RemoteViews(context.packageName, notificationContent.getContentLayout(type))
        contentRemoteViews.setImageViewBitmap(R.id.business_image, img)
        contentRemoteViews.setTextViewText(R.id.business_title, this.resetContentTitle(title,order,type))
        contentRemoteViews.setTextViewText(R.id.business_order, this.resetContentOrder(order,type))

        contentRemoteViews.setOnClickPendingIntent(R.id.check_button, this.getCompletedIntent(id))

        if(type == 1 || type == -1){
            contentRemoteViews.setOnClickPendingIntent(R.id.screenshot_button, this.getScreenshotIntent(uri))
        }

        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(order)
            .setContentIntent(this.getOpenedIntent(pack))
            .setShowWhen(true)
            .addExtras(liveBundle)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()
        notification.contentView = contentRemoteViews
        notificationManager.notify(id, notification)
    }

    private fun resetCapsuleCodeDisplay(order: String, type: Int): String{
        when(type){
            -1 -> return order.replace("取餐柜 ","柜")
            else -> return order
        }
    }

    private fun resetContentTitle(title: String,order: String,type: Int): String{
        when(type){
            3 -> {
                if(order.contains("号柜")){
                    return title + " · " + order.substring(0,3)
                }
                return title
            }
            else -> return title
        }
    }

    private fun resetContentOrder(order: String,type: Int): String{
        when(type){
            3 -> {
                var new_order = order
                if(order.contains("号柜")){
                    new_order = order.substring(3)
                }
                if(new_order.length == 8 && !new_order.contains("-")){
                    new_order = new_order.substring(0,4) + " " + new_order.substring(4)
                }
                return new_order
            }
            else -> return order
        }
    }

    fun cancelLiveNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun Color.getIslandTextColor(): Int{
        // 3. 判断亮度是否低于阈值
        // 如果亮度低于阈值，说明背景太暗，黑色文字会看不清
        if(luminance() < 0.35){
            Log.i("OrderAgents","实况窗文字颜色: 白色")
            return android.R.color.white
        } else {
            Log.i("OrderAgents","实况窗文字颜色: 黑色")
            return android.R.color.black
        }
    }

    private fun getOpenedIntent(pack: String): PendingIntent{
        var intent = context.packageManager.getLaunchIntentForPackage(pack)
        if(intent == null){
            Log.i("OrderAgents","设置主程序 intent")
            intent = Intent(context, MainActivity::class.java)
        }
        Log.i("OrderAgents","设置目标App intent")
        intent.flags = 0
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val requestCode = System.currentTimeMillis().toInt()
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getScreenshotIntent(uri: Uri?): PendingIntent?{
        if(uri == null) return null
        val screenshot = Intent(context, ViewScreenshotActivity::class.java)
        screenshot.setAction(Intent.ACTION_VIEW)
        screenshot.setDataAndType(uri, "image/*")
        screenshot.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        screenshot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            0,
            screenshot,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getCompletedIntent(id: Int): PendingIntent{
        val check = Intent(context, CheckButtonReceiver::class.java)
        check.action = "ACTION_CHECK_CLICK"
        check.putExtra("notice_id",id)
        return PendingIntent.getBroadcast(
            context,
            id,
            check,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
