package com.yxmax.orderagents.receive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.MainActivity
import com.yxmax.orderagents.`object`.OrderRepository

class CheckButtonReceiver : BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_CHECK_CLICK") {
            val extraNumber = intent.getIntExtra("notice_id", 0)
            Log.i("","run remove: " + extraNumber)
            if(extraNumber == 0) return
            OrderRepository.removeCardItem(extraNumber)
            liveNotificationManager.cancelLiveNotification(extraNumber)
        }
    }
}