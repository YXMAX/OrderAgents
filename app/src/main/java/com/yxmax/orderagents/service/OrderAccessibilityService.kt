package com.yxmax.orderagents.service

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.utils.RecognizeProcessor
import com.yxmax.orderagents.utils.sendToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OrderAccessibilityService : AccessibilityService() {
    private var analysisJob: Job? = null

    // 主线程 Handler
    private val handler = Handler(Looper.getMainLooper())

    // 当前延迟任务
    private var debounceRunnable: Runnable? = null

    companion object {
        private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private var instance: OrderAccessibilityService? = null

        fun isRunning(): Boolean {
            return instance != null
        }

        private fun getForegroundPackage(): String?{
            val context = GlobalApplication.context
            val usm = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - (1000 * 30 * 60), time)

            if (stats != null) {
                var recent: UsageStats? = null
                for (usage in stats) {
                    if(usage.packageName.equals("com.yxmax.orderagents")){
                        continue
                    }
                    if (recent == null || usage.getLastTimeUsed() > recent.getLastTimeUsed()) {
                        recent = usage
                    }
                }
                if (recent != null) {
                    return recent.packageName
                }
            }
            return null
        }

        fun requestCapture(){
            serviceScope.launch(Dispatchers.IO) {
                delay(250)
                sendToast("正在进行识别..")
                val packageName = getForegroundPackage()
                if(packageName == null || packageName.contains("input")){
                    return@launch
                }
                instance!!.capture(packageName,true)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this // 服务连接成功，赋值
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null // 服务断开，置空
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        debounceRunnable?.let { handler.removeCallbacks(it) }

        debounceRunnable = Runnable {
            Log.i("OrderAgent","执行OCR识别: " + event.packageName as String)
            capture(event.packageName as String,false)
        }

        handler.postDelayed(debounceRunnable!!, 550)
    }

    private fun capture(pack: String,toast: Boolean) {
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(screenshotResult: ScreenshotResult) {
                    analysisJob = serviceScope.launch(Dispatchers.IO) {
                        processScreenshot(screenshotResult,pack,toast)
                    }
                }
                override fun onFailure(errorCode: Int) {
                    Log.i("OrderAgents","takeScreenShot fail")
                }
            }
        )
    }

    private suspend fun processScreenshot(result: ScreenshotResult,pack: String,toast: Boolean){
        val hardwareBuffer = result.hardwareBuffer
        val colorSpace = result.colorSpace
        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
        if (bitmap == null) {
            return
        }
        RecognizeProcessor.recognizeText(bitmap,pack,toast)
    }

    override fun onInterrupt() {
        // 服务被中断时的处理逻辑
    }

    override fun onDestroy() {
        super.onDestroy()
        debounceRunnable?.let { handler.removeCallbacks(it) }
    }
}