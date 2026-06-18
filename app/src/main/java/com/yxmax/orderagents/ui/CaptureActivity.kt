package com.yxmax.orderagents.ui

import android.app.Activity
import android.os.Bundle
import com.yxmax.orderagents.service.OrderAccessibilityService
import com.yxmax.orderagents.utils.sendToast

class CaptureActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(OrderAccessibilityService.isRunning()){
            OrderAccessibilityService.requestCapture()
        } else {
            sendToast("无障碍服务未开启 无法截图")
        }
        finish()
    }
}