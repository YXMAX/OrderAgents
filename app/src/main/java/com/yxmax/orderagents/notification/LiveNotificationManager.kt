package com.yxmax.orderagents.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.MainActivity
import com.yxmax.orderagents.R
import com.yxmax.orderagents.`object`.OrderInfo
import com.yxmax.orderagents.`object`.OrderRepository
import com.yxmax.orderagents.receive.CheckButtonReceiver
import com.yxmax.orderagents.ui.ViewScreenshotActivity
import com.yxmax.orderagents.utils.RecognizeProcessor
import com.yxmax.orderagents.utils.sendToast
import java.io.File

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
            "取餐码通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "实况通知频道"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showLiveNotificationExample(){
        showLiveNotification(null,"mcdonald",null,"com.yxmax.orderagents")
        sendToast("已发送可点击实况通知")
    }

    private fun Color.getOrderColor(): Int{
        // 3. 判断亮度是否低于阈值
        // 如果亮度低于阈值，说明背景太暗，黑色文字会看不清
        Log.i("OrderAgents","Luminance: " + luminance())
        if(luminance() < 0.35){
            return android.R.color.white
        } else {
            return android.R.color.black
        }
    }

    fun showLiveNotification(uri: Uri?, image: String, order: String?, pack: String) {

        val id = OrderRepository.getNextId()
        var result = ""
        val factoryInfo = RecognizeProcessor.getFactoryName(image,pack)
        val title = factoryInfo.translate
        val img = factoryInfo.image
        val color = factoryInfo.color
        if(id == 1006) return

        if(order == null){
            result = id.toString()
        } else {
            result = order
            if(OrderRepository.hasOrder(result)) return
        }

        // 创建胶囊内容
        val capsuleRemoteViews = RemoteViews(context.packageName, R.layout.live_notification_capsule)
        capsuleRemoteViews.setTextViewText(R.id.capsule_content, result)

        // 创建胶囊配置
        val capsuleBundle = Bundle().apply {
            putInt("notification.live.capsuleStatus", 1)
            putInt("notification.live.capsuleType", 5)
            putString("notification.live.capsuleContent", result)
            putParcelable("notification.live.capsuleIcon", Icon.createWithBitmap(img))

            putInt("notification.live.capsuleBgColor",color.toArgb())
            putInt("notification.live.capsuleContentColor", context.resources.getColor(color.getOrderColor(), null))
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
        val contentRemoteViews = RemoteViews(context.packageName, this.getNotificationContentXML(image))
        if(image.equals("carplate")){
            contentRemoteViews.setTextViewText(R.id.car_plate, result)
        } else {
            contentRemoteViews.setImageViewBitmap(R.id.business_image, img)
            contentRemoteViews.setTextViewText(R.id.business_title, title)
            contentRemoteViews.setTextViewText(R.id.business_order, result)
        }

        val check = Intent(context, CheckButtonReceiver::class.java)
        check.action = "ACTION_CHECK_CLICK"
        check.putExtra("notice_id",id)
        val checkIntent = PendingIntent.getBroadcast(
            context,
            id,
            check,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        contentRemoteViews.setOnClickPendingIntent(R.id.check_button, checkIntent)

        val screenshot = Intent(context, ViewScreenshotActivity::class.java)
        screenshot.setAction(Intent.ACTION_VIEW)
        screenshot.setDataAndType(uri!!, "image/*")
        screenshot.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        screenshot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val screenshotIntent = PendingIntent.getActivity(
            context,
            0,
            screenshot,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        contentRemoteViews.setOnClickPendingIntent(R.id.screenshot_button, screenshotIntent)

        val requestCode = System.currentTimeMillis().toInt()

        val intent = this.getIntent(pack)
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(result)
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .addExtras(liveBundle)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()
        notification.contentView = contentRemoteViews
        notificationManager.notify(id, notification)

        OrderRepository.addCardItem(OrderInfo(img,title,result,id))
    }

    fun cancelLiveNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun getIntent(pack: String): Intent{
        val intent = context.packageManager.getLaunchIntentForPackage(pack)
        if(intent != null){
            Log.i("OrderAgents","设置目标App intent")
            intent.flags = 0
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
        Log.i("OrderAgents","设置主程序 intent")
        val main_intent = Intent(context, MainActivity::class.java)
        main_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        main_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return main_intent
    }

    private fun getNotificationContentXML(image: String): Int{
        if(image.equals("carplate")){
            return R.layout.car_notification_content
        }
        return R.layout.live_notification_content
    }
}
