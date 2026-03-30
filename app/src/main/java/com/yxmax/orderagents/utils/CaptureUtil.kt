package com.yxmax.orderagents.utils

import android.R.attr.action
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.yxmax.orderagents.BaseActivity
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.R
import com.yxmax.orderagents.service.OrderAccessibilityService

class CaptureActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        finishAndRemoveTask()
        if(OrderAccessibilityService.isRunning()){
            OrderAccessibilityService.requestCapture()
        } else {
            sendToast("无障碍服务未开启 无法截图")
        }
    }
}

fun createShortCut(){
    val context = GlobalApplication.context
    if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        sendToast("当前启动器不支持创建快捷方式")
        return
    }
    val shortcutIntent = Intent(context, CaptureActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        action = Intent.ACTION_VIEW
    }
    val shortcutInfo = ShortcutInfoCompat.Builder(context, "quick_capture_shortcut")
        .setShortLabel("截图识别")
        .setLongLabel("OrderAgents 截图识别")
        .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
        .setIntent(shortcutIntent)
        .build()
    val success = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    if (!success) {
        sendToast("创建快捷方式失败")
    }
}
