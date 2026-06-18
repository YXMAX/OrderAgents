package com.yxmax.orderagents

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.yxmax.orderagents.GlobalApplication.Companion.notificationContent
import com.yxmax.orderagents.notification.LiveNotificationManager
import com.yxmax.orderagents.`object`.SimpleNotificationContent
import com.yxmax.orderagents.`object`.NotificationContent
import com.yxmax.orderagents.`object`.StandardNotificationContent

class GlobalApplication : Application() {

    companion object {
        lateinit var context: Context
        lateinit var liveNotificationManager: LiveNotificationManager

        lateinit var packageManager: PackageManager

//        var loosePackageList = hashSetOf<String>("com.sankuai.meituan",
//            "com.jingdong.app.mall",
//            "com.taobao.taobao",
//            "com.dianping.v1",
//            "me.ele")
        lateinit var packageList: HashSet<String>

        lateinit var notificationContent: NotificationContent
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
            "com.lucky.luckyclient",
            "com.sdu.didi.psnger",
            "com.jingyao.easybike",
            "com.huaxiaozhu.rider",
            "com.cainiao.wireless"
            )
        val content = context.getSharedPreferences("notification_content",Context.MODE_PRIVATE)
        when(content.getInt("type",1)){
            1 -> notificationContent = StandardNotificationContent()
            2 -> notificationContent = SimpleNotificationContent()
        }
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

fun switchNotificationContentType(id: Int){
    val sharedPref = GlobalApplication.Companion.context.getSharedPreferences("notification_content", Context.MODE_PRIVATE)
    sharedPref.edit().putInt("type",id).apply()
    when(id){
        1 -> notificationContent = StandardNotificationContent()
        2 -> notificationContent = SimpleNotificationContent()
    }
}

fun getNotificationContentType(): Int{
    val sharedPref = GlobalApplication.Companion.context.getSharedPreferences("notification_content", Context.MODE_PRIVATE)
    return sharedPref.getInt("type",1)
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