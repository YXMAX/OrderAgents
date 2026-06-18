package com.yxmax.orderagents.utils.order

import android.graphics.Bitmap
import android.util.Log
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.utils.processor.RecognizeProcessor.cropScreenshot
import com.yxmax.orderagents.utils.processor.ScreenshotProcessor

object RestaurantOrder {

    const val SPECIAL_REGEX = "\\b[TACMBVOE][O0-9]{3,4}\\b"

    const val NORMAL_REGEX = "(?:(?<=自提:)|(?<=\\s|[\\u7801])|(?<=^))\\d{3,5}(?=\\s|[\\u53d6]|\$)"

    const val LOOSE_REGEX = "(?:(?<=自提:)|(?<=\\s|[\\u7801]))\\d{2,5}(?=\\s|[\\u53d6]|$)"

    const val MCDONALD_GUI_REGEX = "密码\\s*(\\d{6})"

    const val MCDONALD_CODE_REGEX = "\\b(35|36|40|60)\\d{3}\\b"

    const val Letter4_REGEX = "(?<!\\d)\\d{4}(?!\\d)(?=\\s*)"

    const val Letter3_REGEX = "(?<!\\d)\\d{3}(?!\\d)(?=\\s*)"

    const val Manner_REGEX = "(M\\d+)(?=\\.)"

    fun createNotification(text: String, pack: String, factory: String?, bitmapOriginal: Bitmap): Boolean{
        var order: String? = null
        var type = factory
        if(type != null){
            Log.i("OrderAgents","命中品牌: " + type)
            order = this.getOrderByHitFactory(text,type)
        } else {
            Log.i("OrderAgents","未命中品牌 执行包搜索")
            val array = this.getOrderByPackage(text,pack)
            order = array[0]
            type = array[1]
        }
        if(order == null) return false
        Log.i("OrderAgents","识别到取餐码: " + order)
        liveNotificationManager.createNotification(
            ScreenshotProcessor.saveScreenshotToCache(cropScreenshot(bitmapOriginal), order.trim()),
            type!!,
            order.trim(),
            pack,
            1
        )
        return true
    }

    private fun getOrderInMcdonald(text: String): String?{
        Regex(MCDONALD_GUI_REGEX).find(text)?.let{ result ->
            return "取餐柜 " + result.groupValues[1]
        }
        Regex(MCDONALD_CODE_REGEX).find(text)?.let{ result ->
            return result.value
        }
        return null
    }

    private fun getOrderInSpecialRegex(text: String): String?{
        Regex(SPECIAL_REGEX).find(text)?.let{ result ->
            return result.value
        }
        return null
    }

    private fun getOrderInLetter4(text: String): String?{
        Regex(Letter4_REGEX).find(text)?.let{ result ->
            return result.value
        }
        return null
    }

    private fun getOrderInLetter3(text: String): String?{
        Regex(Letter3_REGEX).find(text)?.let{ result ->
            return result.value
        }
        return null
    }

    private fun getOrderInTasting(text: String): String?{
        Regex(NORMAL_REGEX).find(text)?.let{ result ->
            return result.value
        }
        return null
    }

    private fun getOrderInManner(text: String): String?{
        Regex(Manner_REGEX).find(text)?.let{ result ->
            return result.groupValues[1]
        }
        return null
    }

    private fun getResultInWeChatAlipay(text: String): Array<String?>{
        Regex(SPECIAL_REGEX).find(text)?.let{ result ->
            val new_result = result.value.replace("O","0")
            return arrayOf(new_result,this.getFactoryByOrder(new_result))
        }
        Regex(NORMAL_REGEX).find(text)?.let{ result ->
            return arrayOf(result.value.trim(),this.getFactoryByOrder(result.value.trim()))
        }
        return arrayOfNulls(2)
    }

    private fun getResultInAllRegex(text: String): Array<String?>{
        Regex(SPECIAL_REGEX).find(text)?.let{ result ->
            val new_result = result.value.replace("O","0")
            return arrayOf(new_result,this.getFactoryByOrder(new_result))
        }
        Regex(NORMAL_REGEX).find(text)?.let{ result ->
            return arrayOf(result.value.trim(),this.getFactoryByOrder(result.value.trim()))
        }
        Regex(LOOSE_REGEX).find(text)?.let{ result ->
            return arrayOf(result.value.trim(),this.getFactoryByOrder(result.value.trim()))
        }
        return arrayOfNulls(2)
    }

    private fun getOrderByHitFactory(text: String,factory: String): String?{
        when(factory){
            "mcdonald" -> return this.getOrderInMcdonald(text)
            "luckin","guming" -> return this.getOrderInLetter3(text)
            "chapanda","mixue","hsay","xicha","burgerking" -> return this.getOrderInLetter4(text)
            "tasting" -> return this.getOrderInTasting(text)
            "manner" -> return this.getOrderInManner(text)
            "kfc","mollytea","chaji","chaliys","linlee","noyetea","cotti","coco" -> return this.getOrderInSpecialRegex(text)
            else -> return null
        }
    }

    private fun getOrderByPackage(text: String,pack: String): Array<String?>{
        when(pack){
            "com.tencent.mm", "com.eg.android.AlipayGphone"  -> {
                return this.getResultInWeChatAlipay(text)
            }
            "com.mcdonalds.gma.cn" -> return arrayOf(this.getOrderInMcdonald(text),"mcdonald")
            "com.yek.android.kfc.activitys" -> return arrayOf(this.getOrderInSpecialRegex(text),"kfc")
            "com.lucky.luckyclient" -> return arrayOf(this.getOrderInLetter3(text),"luckin")
            else -> {
                return this.getResultInAllRegex(text)
            }
        }
    }

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
                if(order.startsWith("35") || order.startsWith("60") || order.startsWith("40")){
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

//    private fun handleTextToOrder(text: String,pack: String): String?{
//        var result = Regex(SPECIAL_REGEX).find(text)
//        if(result != null){
//            return result.value.replace("O","0")
//        }
//        result = Regex(this.getRegexByPackage(pack)).find(text)
//        if(result != null){
//            return result.value.trim()
//        }
//        return null
//    }
//
//    private fun getRegexByPackage(pack: String): String {
//        if(GlobalApplication.Companion.loosePackageList.contains(pack)){
//            return LOOSE_REGEX
//        }
//        return NORMAL_REGEX
//    }

}