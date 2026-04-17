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

        var loosePackageList = hashSetOf<String>("com.sankuai.meituan",
            "com.jingdong.app.mall",
            "com.taobao.taobao",
            "com.dianping.v1",
            "me.ele")
        lateinit var packageList : HashSet<String>
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        liveNotificationManager = LiveNotificationManager()
        Companion.packageManager = context.packageManager
        packageList = hashSetOf("com.tencent.mm",
            "com.eg.android.AlipayGphone",
            "com.mcdonalds.gma.cn",
            "com.yek.android.kfc.activitys",
            "com.lucky.luckyclient"
            )
        val sharedPref = context.getSharedPreferences("app_enabled", Context.MODE_PRIVATE)
        val iterator = packageList.iterator()
        while (iterator.hasNext()) {
            val name = iterator.next()
            if(!sharedPref.getBoolean(name,true)){
                iterator.remove()
            }
        }
    }
}

fun isAppEnabled(name: String): Boolean{
    return GlobalApplication.packageList.contains(name)
}

fun switchAppList(name: String,bool: Boolean){
    if(bool){
        GlobalApplication.packageList.add(name)
    } else {
        GlobalApplication.packageList.remove(name)
    }
    val sharedPref = GlobalApplication.Companion.context.getSharedPreferences("app_enabled", Context.MODE_PRIVATE)
    sharedPref.edit().putBoolean(name,bool).apply()
}