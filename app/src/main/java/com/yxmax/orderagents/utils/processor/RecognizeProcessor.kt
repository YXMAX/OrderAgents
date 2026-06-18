package com.yxmax.orderagents.utils.processor

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie
import com.yxmax.orderagents.GlobalApplication
import com.yxmax.orderagents.R
import com.yxmax.orderagents.`object`.FactoryInfo
import com.yxmax.orderagents.`object`.toBitmap
import com.yxmax.orderagents.ui.theme.BurgerKing
import com.yxmax.orderagents.ui.theme.CaiNiao
import com.yxmax.orderagents.ui.theme.Carplate
import com.yxmax.orderagents.ui.theme.ChaPanda
import com.yxmax.orderagents.ui.theme.Chaliys
import com.yxmax.orderagents.ui.theme.Coco
import com.yxmax.orderagents.ui.theme.Cotti
import com.yxmax.orderagents.ui.theme.DiDi
import com.yxmax.orderagents.ui.theme.FengChao
import com.yxmax.orderagents.ui.theme.Haluo
import com.yxmax.orderagents.ui.theme.Hsay
import com.yxmax.orderagents.ui.theme.HuaXZ
import com.yxmax.orderagents.ui.theme.KFC
import com.yxmax.orderagents.ui.theme.LINLEE
import com.yxmax.orderagents.ui.theme.Luckin
import com.yxmax.orderagents.ui.theme.MaMa
import com.yxmax.orderagents.ui.theme.McDonald
import com.yxmax.orderagents.ui.theme.MollyTea
import com.yxmax.orderagents.ui.theme.Noyetea
import com.yxmax.orderagents.ui.theme.TasTing
import com.yxmax.orderagents.ui.theme.TuXi
import com.yxmax.orderagents.ui.theme.Yidd
import com.yxmax.orderagents.utils.order.CarplateOrder
import com.yxmax.orderagents.utils.order.DeliveryOrder
import com.yxmax.orderagents.utils.order.RestaurantOrder
import com.yxmax.orderagents.utils.sendToast
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object RecognizeProcessor {

    private val filter_list by lazy {
        setOf("0","1","2","end")
    }

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
            "等待车主到达" to "1",
            "等待车主出发" to "1",
            "取件码" to "2",
            "取件" to "2",
            "订单已完成" to "end",
            "订单己完成" to "end",
            "茶百道" to "chapanda",
            "熊猫币" to "chapanda",
            "茉莉奶白" to "mollytea",
            "Molly " to "mollytea",
            "MOLLY " to "mollytea",
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
            "取素号)" to "tasting",
            "参与塔塔" to "tasting",
            "LINLEE" to "linlee",
            "菜鸟驿站" to "cainiao",
            "菜乌驿站" to "cainiao",
            "兔喜生活" to "tuxi",
            "免喜生活" to "tuxi",
            "妈妈驿站" to "mama",
            "丰巢" to "fengchao"
        )
        val trie = AhoCorasickDoubleArrayTrie<String>()
        trie.build(map)
        trie
    }

    // 1:取餐码 2:车牌号 3:取件码
    private val factoryMap = mapOf(
        "tasting" to FactoryInfo("塔斯汀", TasTing, R.drawable.tasting),
        "mcdonald" to FactoryInfo("麦当劳", McDonald, R.drawable.mcdonald),
        "kfc" to FactoryInfo("肯德基", KFC, R.drawable.kfc),
        "mollytea" to FactoryInfo("茉莉奶白", MollyTea, R.drawable.mollytea),
        "xicha" to FactoryInfo("喜茶", MollyTea, R.drawable.xicha),
        "mixue" to FactoryInfo("蜜雪冰城", TasTing, R.drawable.mixue),
        "luckin" to FactoryInfo("瑞幸咖啡", Luckin, R.drawable.luckin),
        "chaji" to FactoryInfo("霸王茶姬", TasTing, R.drawable.chaji),
        "chapanda" to FactoryInfo("茶百道", ChaPanda, R.drawable.chapanda),
        "coco" to FactoryInfo("COCO", Coco, R.drawable.coco),
        "cotti" to FactoryInfo("库迪咖啡", Cotti, R.drawable.cotti),
        "guming" to FactoryInfo("古茗", MollyTea, R.drawable.guming),
        "manner" to FactoryInfo("Manner", MollyTea, R.drawable.manner),
        "yidd" to FactoryInfo("一點點", Yidd, R.drawable.yidd),
        "noyetea" to FactoryInfo("爷爷不泡茶", Noyetea, R.drawable.noyetea),
        "chaliys" to FactoryInfo("茶理宜世", Chaliys, R.drawable.chaliys),
        "hsay" to FactoryInfo("沪上阿姨", Hsay, R.drawable.hsay),
        "burgerking" to FactoryInfo("汉堡王", BurgerKing, R.drawable.burgerking),
        "linlee" to FactoryInfo("LINLEE", LINLEE, R.drawable.linlee),
        "didi" to FactoryInfo("滴滴出行", DiDi,R.drawable.didi),
        "haluo" to FactoryInfo("哈啰", Haluo,R.drawable.haluo),
        "huaxz" to FactoryInfo("花小猪打车", HuaXZ,R.drawable.huaxz),
        "cainiao" to FactoryInfo("菜鸟", CaiNiao,R.drawable.cainiao),
        "tuxi" to FactoryInfo("兔喜生活", TuXi,R.drawable.tuxi),
        "fengchao" to FactoryInfo("丰巢", FengChao,R.drawable.fengchao),
        "mama" to FactoryInfo("妈妈驿站", MaMa,R.drawable.mama)
    )


    fun getFactory(key: String, pack: String): FactoryInfo {
        if(factoryMap.containsKey(key)){
            return factoryMap.get(key)!!
        }
        return this.getAppIconByPackage(pack)
    }

    fun getAppIconByPackage(pack: String): FactoryInfo {
        val packageManager = GlobalApplication.Companion.packageManager
        val appInfo = packageManager.getApplicationInfo(pack, 0)
        val appName = packageManager.getApplicationLabel(appInfo).toString()
        val icon = packageManager.getApplicationIcon(appInfo).toBitmap()
        Log.i("OrderAgent","图标大小: " + icon.width + "x" + icon.height)
        val color = icon.getPixel(24,24)
        Log.i("OrderAgent","颜色: " + color)
        return FactoryInfo(appName, Color(color), icon)
    }

    suspend fun recognizeText(bitmap: Bitmap, pack: String, toast: Boolean){
        val text = this.processByOCR(bitmap).text.trimIndent()
        Log.i("OrderAgents","前台应用: " + pack)
        Log.i("OrderAgents","文字识别结果: " + text)

        if(!this.processHitText(bitmap, pack, text)){
            this.sendFailedToast(toast)
            return
        }
    }

    private fun sendFailedToast(toast: Boolean){
        if(toast){
            sendToast("未识别到有效的物品码")
        }
    }

    private fun getHitFactory(set: MutableSet<String>): String?{
        set.removeAll { it in filter_list }
        return set.firstOrNull()
    }

    private fun processHitText(bitmapOriginal: Bitmap,pack: String,text: String): Boolean{
        val list = orderRecognizer.parseText(text).map{it.value}.toMutableSet()
        if(list.isEmpty()) return false
        when{
            "0" in list -> {
                Log.i("OrderAgents","进入识别第一阶段: 取餐码")
                if("end" in list){
                    Log.i("OrderAgents","该订单已完成")
                    sendToast("该订单已完成")
                    return true
                }
                return RestaurantOrder.createNotification(text,pack,this.getHitFactory(list),bitmapOriginal)
            }

            "1" in list -> {
                Log.i("OrderAgents","进入识别第一阶段: 车牌号")
                return CarplateOrder.createNotification(text,pack)
            }

            "cainiao" in list -> {
                Log.i("OrderAgents","进入识别第一阶段: 菜鸟驿站取件码")
                return DeliveryOrder.createNotification(text,pack,"cainiao")
            }

            "tuxi" in list -> {
                Log.i("OrderAgents","进入识别第一阶段: 兔喜取件码")
                return DeliveryOrder.createNotification(text,pack,"tuxi")
            }

            "fengchao" in list -> {
                Log.i("OrderAgents","进入识别第一阶段: 丰巢取件码")
                return DeliveryOrder.createNotification(text,pack,"fengchao")
            }

            "mama" in list -> {
                Log.i("OrderAgents","进入识别第一阶段: 妈妈驿站取件码")
                return DeliveryOrder.createNotification(text,pack,"mama")
            }

            "2" in list -> {
                Log.i("OrderAgents","进入识别第一阶段: 标准取件码")
                return DeliveryOrder.createNotification(text,pack,"default")
            }

            else -> return false
        }
    }

    fun cropScreenshot(bitmap: Bitmap): Bitmap {
        return Bitmap.createBitmap(bitmap, 0, 125, bitmap.width,bitmap.height - 275)
    }
    private suspend fun processByOCR(bitmap: Bitmap): Text =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(cropScreenshot(bitmap), 0)
            ocrRecognizer.process(image)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
}