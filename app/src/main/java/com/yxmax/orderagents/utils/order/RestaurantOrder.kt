package com.yxmax.orderagents.utils.order

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.text.Text
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.utils.processor.ScreenshotProcessor

object RestaurantOrder {

    /**
     * @param bitmapCropped 已裁剪的截图, 与 [vision] 中 boundingBox 的坐标系一致
     * @param factory       关键字命中的品牌, 未命中时为 null
     * @param anchored      是否命中明确的取餐码锚点, false 时提取器要求更高置信度
     */
    fun createNotification(
        vision: Text,
        pack: String,
        factory: String?,
        bitmapCropped: Bitmap,
        anchored: Boolean = true
    ): Boolean{
        var type = factory
        if(type != null && OrderCodeExtractor.hasRule(type)){
            Log.i("OrderAgents","命中品牌: " + type)
        } else {
            if(type != null){
                Log.i("OrderAgents","品牌 " + type + " 无取码规则 降级为包搜索")
            } else {
                Log.i("OrderAgents","未命中品牌 执行包搜索")
            }
            type = null
        }

        val candidate = OrderCodeExtractor.extract(
            vision,
            type ?: this.getRuleByPackage(pack),
            bitmapCropped.height,
            anchored
        ) ?: return false

        val order = candidate.code.trim()
        if(order.isEmpty()) return false
        Log.i("OrderAgents","识别到取餐码: " + order + " (得分 " + candidate.score + " 来源行 " + candidate.line + ")")

        liveNotificationManager.createNotification(
            ScreenshotProcessor.saveScreenshotToCache(bitmapCropped, order),
            type ?: this.getFactoryByOrder(order),
            order,
            pack,
            1
        )
        return true
    }

    /** 品牌未知时, 按前台包名挑一套通用规则 */
    private fun getRuleByPackage(pack: String): String{
        return when(pack){
            "com.mcdonalds.gma.cn" -> "mcdonald"
            "com.yek.android.kfc.activitys" -> "kfc"
            "com.lucky.luckyclient" -> "luckin"
            // 微信/支付宝小程序页面噪声多, 用严格规则
            "com.tencent.mm", "com.eg.android.AlipayGphone" -> OrderCodeExtractor.BRAND_STRICT
            else -> OrderCodeExtractor.BRAND_LOOSE
        }
    }

    /** 反过来用码的形态猜品牌, 决定通知的图标与配色 */
    private fun getFactoryByOrder(order: String): String{
        when(order.length){
            4 -> {
                when {
                    order.startsWith("A") -> return "cotti"
                    order.startsWith("C") -> return "coco"
                    order.startsWith("M") -> return "manner"
                    order.startsWith("B") || order.startsWith("V") -> return "yidd"
                    order.startsWith("2") -> return "hsay"
                    order.startsWith("9") -> return "burgerking"
                }
            }
            5 -> {
                if(order.startsWith("35") || order.startsWith("36") || order.startsWith("60") || order.startsWith("40")){
                    return "mcdonald"
                } else if(order.startsWith("A")){
                    return "kfc"
                } else if(order.startsWith("T0")){
                    return "chaji"
                } else if(order.startsWith("T8")){
                    return "noyetea"
                }
            }
        }
        return "default"
    }
}
