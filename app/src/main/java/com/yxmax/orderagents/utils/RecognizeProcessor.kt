package com.yxmax.orderagents.utils

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.R
import com.yxmax.orderagents.`object`.FactoryInfo
import com.yxmax.orderagents.`object`.toBitmap
import com.yxmax.orderagents.ui.theme.BurgerKing
import com.yxmax.orderagents.ui.theme.ChaPanda
import com.yxmax.orderagents.ui.theme.Chaliys
import com.yxmax.orderagents.ui.theme.Coco
import com.yxmax.orderagents.ui.theme.Cotti
import com.yxmax.orderagents.ui.theme.Hsay
import com.yxmax.orderagents.ui.theme.KFC
import com.yxmax.orderagents.ui.theme.LINLEE
import com.yxmax.orderagents.ui.theme.Luckin
import com.yxmax.orderagents.ui.theme.McDonald
import com.yxmax.orderagents.ui.theme.MollyTea
import com.yxmax.orderagents.ui.theme.Noyetea
import com.yxmax.orderagents.ui.theme.TasTing
import com.yxmax.orderagents.ui.theme.Yidd
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object RecognizeProcessor {

    const val SPECIAL_REGEX = "\\b[TACMBV][O0-9]{3,4}\\b"

    const val NORMAL_REGEX = "(?:(?<=自提:)|(?<=\\s|[\\u7801]))\\d{3,5}(?=\\s|[\\u53d6]|$)"

    const val LOOSE_REGEX = "(?:(?<=自提:)|(?<=\\s|[\\u7801]))\\d{2,5}(?=\\s|[\\u53d6]|$)"

    const val PLATE_REGEX = "(?i).*?([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z](?:[A-HJ-NP-Z0-9]{5,6}))(?:[·\\-\\s]*(?:蓝牌|绿牌|黄牌|黑牌|白牌|新能源|纯电|混动|大型|小型|车)*)?.*?"

    private val ocrRecognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    private val orderRecognizer by lazy{
        val map = mapOf(
            "订单详情" to "0",
            "取餐码" to "0",
            "取餐号" to "0",
            "取单号" to "0",
            "取单码" to "0",
            "订单号" to "0",
            "取茶码" to "0",
            "取荼码" to "0",
            "司机正在赶来" to "1",
            "订单已完成" to "end",
            "订单己完成" to "end",
            "茶百道" to "chapanda",
            "熊猫币" to "chapanda",
            "茉莉奶白" to "mollytea",
            "Molly " to "mollytea",
            "蜜雪冰城" to "mixue",
            "餐码给好友" to "luckin",
            "霸王茶姬" to "chaji",
            "CoCo都可" to "coco",
            "KFC" to "kfc",
            "塔斯汀" to "tasting",
            "喜茶" to "xicha",
            "瑞幸咖啡" to "luckin",
            "库迪咖啡" to "cotti",
            "古茗" to "guming",
            "Manner" to "manner",
            "爷爷不泡茶" to "noyetea",
            "茶理宜世" to "chaliys",
            "沪上阿姨" to "hsay",
            "汉堡王" to "burgerking",
            "制茶中" to "xicha",
            "取茶号" to "guming",
            "麦当劳" to "mcdonald",
            "取實号)" to "tasting",
            "取餐号)" to "tasting",
            "LINLEE" to "linlee"
        )
        val trie = AhoCorasickDoubleArrayTrie<String>()
        trie.build(map)
        trie
    }

    private val factoryMap = mapOf(
        "tasting" to FactoryInfo("塔斯汀", TasTing, R.drawable.tasting),
        "mcdonald" to FactoryInfo("麦当劳", McDonald, R.drawable.mcdonald),
        "kfc" to FactoryInfo("肯德基", KFC, R.drawable.kfc),
        "mollytea" to FactoryInfo("茉莉奶白", MollyTea,R.drawable.mollytea),
        "xicha" to FactoryInfo("喜茶",MollyTea,R.drawable.xicha),
        "mixue" to FactoryInfo("蜜雪冰城",TasTing,R.drawable.mixue),
        "luckin" to FactoryInfo("瑞幸咖啡",Luckin,R.drawable.luckin),
        "chaji" to FactoryInfo("霸王茶姬",TasTing,R.drawable.chaji),
        "chapanda" to FactoryInfo("茶百道", ChaPanda,R.drawable.chapanda),
        "coco" to FactoryInfo("COCO", Coco,R.drawable.coco),
        "cotti" to FactoryInfo("库迪咖啡", Cotti,R.drawable.cotti),
        "guming" to FactoryInfo("古茗",MollyTea,R.drawable.guming),
        "manner" to FactoryInfo("Manner",MollyTea,R.drawable.manner),
        "yidd" to FactoryInfo("一點點", Yidd,R.drawable.yidd),
        "noyetea" to FactoryInfo("爷爷不泡茶", Noyetea,R.drawable.noyetea),
        "chaliys" to FactoryInfo("茶理宜世", Chaliys,R.drawable.chaliys),
        "hsay" to FactoryInfo("沪上阿姨", Hsay,R.drawable.hsay),
        "burgerking" to FactoryInfo("汉堡王", BurgerKing,R.drawable.burgerking),
        "linlee" to FactoryInfo("LINLEE", LINLEE,R.drawable.linlee),
        "carplate" to FactoryInfo("车牌号",Color(116, 238, 147, 255),R.drawable.carplate)
    )


    fun getFactoryName(key: String,pack: String): FactoryInfo {
        if(factoryMap.containsKey(key)){
            return factoryMap.get(key)!!
        }
        return this.getDefaultIconByPackage(pack)
    }

    fun getDefaultIconByPackage(pack: String): FactoryInfo{
        val packageManager = GlobalApplication.packageManager
        val appInfo = packageManager.getApplicationInfo(pack, 0)
        val appName = packageManager.getApplicationLabel(appInfo).toString()
        val icon = packageManager.getApplicationIcon(appInfo).toBitmap()
        Log.i("OrderAgent","图标大小: " + icon.width + "x" + icon.height)
        val color = icon.getPixel(24,24)
        Log.i("OrderAgent","颜色: " + color)
        return FactoryInfo(appName, Color(color),icon)
    }

    suspend fun recognizeText(bitmap: Bitmap,pack: String,toast: Boolean){
        val text = this.processByOCR(bitmap).text
        Log.i("OrderAgents","前台应用: " + pack)
        Log.i("OrderAgents","文字识别结果: " + text)

        val parseText = orderRecognizer.parseText(text)

        if(!parseText.isEmpty()){
            if(parseText.any { it.value == "0" }){
                Log.i("OrderAgents","进入识别第一阶段: 取餐码")
                if(parseText.any { it.value == "end" }){
                    Log.i("OrderAgents","该订单已完成")
                    sendToast("该订单已完成")
                    return
                }
                val filter = parseText.filterNot { it.value == "0" }
                val order = this.extractOrder(text,pack)
                if(order == null){
                    if(toast){
                        sendToast("未识别到有效的物品码")
                    }
                    return
                }
                Log.i("OrderAgents","识别到取餐码: " + order)
                if(filter.isEmpty()){
                    Log.i("OrderAgents","未在OCR文字中获取到具体品牌")
                    this.recognizeFactory(cropScreenshot(bitmap),"default",order,pack)
                    return
                }
                Log.i("OrderAgents","成功在OCR文字中获取品牌: " + filter[0].value)
                this.recognizeFactory(cropScreenshot(bitmap),filter[0].value,order,pack)
                return
            } else if(parseText.any { it.value == "1" }){
                Log.i("OrderAgents","进入识别第一阶段: 车牌号")
                val plate = this.getCarPlate(text)
                if(plate == null){
                    if(toast){
                        sendToast("未识别到有效的物品码")
                    }
                    return
                }
                Log.i("OrderAgents","识别到车牌号: " + plate)
                this.insertCarPlate(cropScreenshot(bitmap),plate,pack)
                return
            }
        }
        if(toast){
            sendToast("未识别到有效的物品码")
        }
    }

    fun saveScreenshotToCache(bitmap: Bitmap, order: String): Uri? {
        try {
            val context = GlobalApplication.context
            // 1. 获取缓存目录
            val cachePath = File(context.getCacheDir(), "images")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }

            Log.i("OrderAgents","缓存的图片数量: " + cachePath.listFiles().size.toString())

            // 2. 创建文件
            val file = File(cachePath, order + ".png")
            if(file.exists()){
                file.delete()
            }
            val stream = FileOutputStream(file)


            // 3. 写入数据
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
            stream.flush()
            stream.close()

            Log.i("OrderAgents","记录的文件名: " + file.name)

            // 4. 获取Uri (使用 FileProvider 以适配 Android 7.0+)
            return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun removeScreenshotFromCache(order: String) {
        try {
            val context = GlobalApplication.context
            // 1. 重建文件路径 (必须与保存时的路径逻辑一致)
            val cachePath = File(context.getCacheDir(), "images")
            val file = File(cachePath, order + ".png")

            // 2. 检查文件是否存在并删除
            if (file.exists()) {
                if (file.delete()) {
                    Log.i("OrderAgents","截图缓存删除成功: $order")
                } else {
                    Log.i("OrderAgents","截图缓存删除失败: $order")
                }
                return
            }
            Log.i("OrderAgents","截图缓存不存在")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractOrder(text: String,pack: String): String? {
        val result = this.getSpecialOrder(text)
        if(result != null) return result
        val regex = this.getRegex(pack)
        val num = Regex(regex).find(text)
        if(num != null) return num.value.trim()
        return null
    }

    private fun getRegex(pack: String): String {
        if(GlobalApplication.loosePackageList.contains(pack)){
            return LOOSE_REGEX
        }
        return NORMAL_REGEX
    }

    private fun getCarPlate(text: String): String? {
        val new_text = text.trim().uppercase()
        val result = Regex(PLATE_REGEX).find(new_text)
        if(result != null){
            return result.value.replace("-","").replace("·","")
        }
        return null
    }

    private fun getSpecialOrder(text: String): String? {
        val result = Regex(SPECIAL_REGEX).find(text)
        if(result != null){
            return result.value.replace("O","0")
        }
        return null
    }

    private fun cropImage(bitmap: Bitmap): Bitmap{
        return Bitmap.createBitmap(bitmap, 0, 100, bitmap.width,bitmap.height - 250)
    }

    private fun cropScreenshot(bitmap: Bitmap): Bitmap{
        return Bitmap.createBitmap(bitmap, 0, 150, bitmap.width,bitmap.height - 300)
    }

    private suspend fun processByOCR(bitmap: Bitmap): Text =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(cropImage(bitmap), 0)
            ocrRecognizer.process(image)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }

    private fun recognizeFromOrder(order: String): String{
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

    private fun recognizeFactory(bitmap: Bitmap,factory: String,order: String,pack: String){
        var result = factory
        when(pack){
            "com.tencent.mm", "com.eg.android.AlipayGphone"  -> {
                if(result.equals("default")){
                    result = this.recognizeFromOrder(order)
                }
            }
            "com.mcdonalds.gma.cn" -> {
                if(order.startsWith("35") || order.startsWith("60") || order.startsWith("40")){
                    result = "mcdonald"
                } else {
                    return
                }
            }
            "com.yek.android.kfc.activitys" -> {
                if(!order.startsWith("A")) {
                    return
                }
                result = "kfc"
            }
            "com.lucky.luckyclient" -> {
                if(order.length != 3) {
                    return
                }
                result = "luckin"
            }
            else -> {
                if(result.equals("default")){
                    result = this.recognizeFromOrder(order)
                }
            }
        }
        if(!result.equals("default")){
            Log.i("OrderAgents","成功通过取餐码获取品牌: " + result)
        }
        val imageUri = this.saveScreenshotToCache(bitmap, order.trim())
        liveNotificationManager.showLiveNotification(imageUri,result,order.trim(),pack)
    }

    private fun insertCarPlate(bitmap: Bitmap,plate: String,pack: String){
        val imageUri = this.saveScreenshotToCache(bitmap, plate.trim())
        liveNotificationManager.showLiveNotification(imageUri,"carplate",plate.trim(),pack)
    }
}