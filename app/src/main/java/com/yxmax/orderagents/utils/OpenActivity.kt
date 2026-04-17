package com.yxmax.orderagents.utils

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.yxmax.orderagents.AppSettingsActivity
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.MainActivity
import com.yxmax.orderagents.SettingsActivity

fun openSettingsActivity(){
    val context = GlobalApplication.context
    val intent = Intent(context, SettingsActivity::class.java)
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