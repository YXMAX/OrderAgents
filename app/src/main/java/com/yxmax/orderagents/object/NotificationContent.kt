package com.yxmax.orderagents.`object`

import com.yxmax.orderagents.R

interface NotificationContent {

    fun getContentLayout(type: Int): Int

}

class StandardNotificationContent: NotificationContent {

    override fun getContentLayout(type: Int): Int {
        when(type){
            -1 -> return R.layout.mcdonald_notification_content
            1 -> return R.layout.standard_notification_content
            else -> return R.layout.general_notification_content
        }
    }

}

class SimpleNotificationContent: NotificationContent {

    override fun getContentLayout(type: Int): Int {
        when(type){
            -1 -> return R.layout.mcdonald_notification_content
            1 -> return R.layout.simple_notification_content
            else -> return R.layout.general_notification_content
        }
    }
}