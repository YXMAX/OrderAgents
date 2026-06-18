package com.yxmax.orderagents.utils.order

import android.util.Log
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager

object CarplateOrder {

    const val PLATE_REGEX = "(?i).*?([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z](?:[A-HJ-NP-Z0-9]{5,6}))(?:[·\\-\\s]*(?:蓝牌|绿牌|黄牌|黑牌|白牌|新能源|纯电|混动|大型|小型|车)*)?.*?"

    fun createNotification(text: String,pack: String): Boolean{
        val order = this.findCarPlate(text)
        if(order == null) return false
        Log.i("OrderAgents","识别到车牌号: " + order)
        liveNotificationManager.createNotification(null,this.getCarplateFactory(pack),order,pack,2)
        return true
    }

    private fun findCarPlate(text: String): String? {
        val result = Regex(PLATE_REGEX).find(text.trim().uppercase())
        if(result != null){
            val r = result.value.replace("-","").replace("·","").trim()
            if(!r.contains("·")){
                return r.substring(0,2) + "·" + r.substring(2)
            }
        }
        return null
    }

    private fun getCarplateFactory(pack: String): String{
        when(pack){
            "com.sdu.didi.psnger" -> return "didi"
            "com.jingyao.easybike" -> return "haluo"
            "com.huaxiaozhu.rider" -> return "huaxz"
            else -> return "default"
        }
    }
}