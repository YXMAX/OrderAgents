package com.yxmax.orderagents

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.yxmax.orderagents.notification.LiveNotificationManager

class GlobalApplication : Application() {

    companion object {
        lateinit var context: Context
        lateinit var liveNotificationManager: LiveNotificationManager

        lateinit var packageManager: PackageManager
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        liveNotificationManager = LiveNotificationManager()
        Companion.packageManager = context.packageManager
    }
}