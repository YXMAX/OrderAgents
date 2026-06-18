package com.yxmax.orderagents.utils

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.yxmax.orderagents.AppSettingsActivity
import com.yxmax.orderagents.ContentSettingsActivity
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.R
import com.yxmax.orderagents.SettingsActivity
import com.yxmax.orderagents.ui.CaptureActivity
import com.yxmax.orderagents.ui.ViewScreenshotActivity

fun openScreenshotActivity(uri: Uri?){
    if(uri == null) return
    val context = GlobalApplication.context
    val screenshot = Intent(context, ViewScreenshotActivity::class.java)
    screenshot.setAction(Intent.ACTION_VIEW)
    screenshot.setDataAndType(uri, "image/*")
    screenshot.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    screenshot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(screenshot)
}

fun openSettingsActivity(){
    val context = GlobalApplication.context
    val intent = Intent(context, SettingsActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openContentSettingsActivity(){
    val context = GlobalApplication.context
    val intent = Intent(context, ContentSettingsActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openAutoRecognizeSettingsActivity(){
    val context = GlobalApplication.context
    val intent = Intent(context, AppSettingsActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openAppSettings() {
    val context = GlobalApplication.context
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openAccessibilitySettings() {
    val context = GlobalApplication.context
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openStorageAccess(){
    if (!Environment.isExternalStorageManager()) {
        val context = GlobalApplication.context
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return
    }
    sendToast("读取存储权限已开启")
}

fun openUsageAccess(){
    val context = GlobalApplication.context
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

fun createShortCut(){
    val context = GlobalApplication.context
    if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        sendToast("当前启动器不支持创建快捷方式")
        return
    }
    val shortcutIntent = Intent(context, CaptureActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
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