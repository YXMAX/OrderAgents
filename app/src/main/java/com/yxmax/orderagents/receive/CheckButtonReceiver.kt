package com.yxmax.orderagents.receive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.`object`.OrderRepository
import com.yxmax.orderagents.utils.processor.ScreenshotProcessor

class CheckButtonReceiver : BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_CHECK_CLICK") {
            val extraNumber = intent.getIntExtra("notice_id", 0)
            Log.i("OrderAgents","执行消息ID删除: " + extraNumber)
            if(extraNumber == 0) return
            val screenshot = OrderRepository.getScreenshot(extraNumber)
            if(screenshot != null){
                ScreenshotProcessor.removeScreenshotFromCache(screenshot)
            }
            OrderRepository.removeCardItem(extraNumber)
            liveNotificationManager.cancelLiveNotification(extraNumber)
        }
    }
}