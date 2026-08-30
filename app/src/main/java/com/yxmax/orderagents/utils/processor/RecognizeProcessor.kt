package com.yxmax.orderagents.utils.processor

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
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
import com.yxmax.orderagents.utils.order.OrderKeyword
import com.yxmax.orderagents.utils.order.RestaurantOrder
import com.yxmax.orderagents.utils.sendToast
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object RecognizeProcessor {

    private val ocrRecognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
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
        val cropped = cropScreenshot(bitmap)
        val vision = this.processByOCR(cropped)
        val text = vision.text.trimIndent()
        Log.i("OrderAgents","前台应用: " + pack)
        Log.i("OrderAgents","文字识别结果: " + text)

        if(!this.processHitText(cropped, pack, text, vision)){
            this.sendFailedToast(toast)
            return
        }
    }

    private fun sendFailedToast(toast: Boolean){
        if(toast){
            sendToast("未识别到有效的物品码")
        }
    }

    private fun processHitText(bitmapCropped: Bitmap,pack: String,text: String,vision: Text): Boolean{
        val hit = OrderKeyword.detect(text) ?: return false
        when(hit.stage){
            OrderKeyword.STAGE_RESTAURANT -> {
                Log.i("OrderAgents","进入识别第一阶段: 取餐码 (品牌 " + hit.brand + " 锚点 " + hit.anchored + ")")
                // 「订单已完成」不代表取不到码 —— 喜茶/霸王茶姬完成后仍然显示取餐号,
                // 所以先尝试提取, 真的没有码时才提示已完成
                if(RestaurantOrder.createNotification(vision,pack,hit.brand,bitmapCropped,hit.anchored)){
                    return true
                }
                if(hit.finished){
                    Log.i("OrderAgents","该订单已完成")
                    sendToast("该订单已完成")
                    return true
                }
                return false
            }

            OrderKeyword.STAGE_CARPLATE -> {
                Log.i("OrderAgents","进入识别第一阶段: 车牌号")
                return CarplateOrder.createNotification(text,pack)
            }

            OrderKeyword.STAGE_DELIVERY -> {
                val factory = hit.brand ?: "default"
                Log.i("OrderAgents","进入识别第一阶段: 取件码 (" + factory + ")")
                return DeliveryOrder.createNotification(text,pack,factory)
            }

            else -> return false
        }
    }

    fun cropScreenshot(bitmap: Bitmap): Bitmap {
        return Bitmap.createBitmap(bitmap, 0, 125, bitmap.width,bitmap.height - 275)
    }

    /** 传入的必须是已裁剪过的图, 保证 boundingBox 与该图坐标系一致 */
    private suspend fun processByOCR(bitmap: Bitmap): Text =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            ocrRecognizer.process(image)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
}