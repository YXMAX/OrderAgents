package com.yxmax.orderagents.utils.order

import android.util.Log
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager

object DeliveryOrder {

    private const val D = OcrText.DIGIT
    private const val NB = OcrText.NOT_BEFORE
    private const val NA = OcrText.NOT_AFTER

    private val CAINIAO_REGEX = Regex("$D{1,3}-$D-$D{4}")

    private val TUXI_REGEX = Regex(
        "$NB(?:[A-Z][A-Z0-9]-$D{4,5}" +
            "|$D{1,3}-$D{1,2}-$D{5}" +
            "|$D{1,3}-$D-$D{4}" +
            "|$D{7,8}" +
            "|[A-Z]-$D{1,2}-$D{5,6}" +
            "|$D{4}-$D{4})$NA"
    )

    private val TUXI_GUI_NUMBER_REGEX = Regex("$NB($D+)号\\w{0,2}[兔免]喜快递")

    private val TUXI_GUI_REGEX = Regex("(?:(?<=凭|取件码|为)|$NB)[A-Z]?$D{5,6}$NA")

    private val FENGCHAO_REGEX = Regex("(?:(?<=凭|取件码|码)|$NB)(?:$D{6,8}|$D{4}\\s$D{4})$NA")

    private val FENGCHAO_GUI_REGEX = Regex("($D+)号.*?丰巢|丰巢.*?($D+)号")

    private val MAMA_REGEX = Regex("$NB(?:$D{2}-$D{3,4}|$D{1,3}-$D{1,2}-$D{2,4}|$D{5,8})$NA")

    fun createNotification(text: String,pack: String,factory: String): Boolean{
        val orders = this.getDeliveryOrder(text,factory)
        if(orders.isEmpty()) return false
        for(i in orders){
            Log.i("OrderAgents","识别到取件码: " + i)
            liveNotificationManager.createNotification(null,factory,i,pack,3)
        }
        return true
    }

    private fun getDeliveryOrder(text: String, factory: String): List<String>{
        when(factory){
            "cainiao" -> return this.findCodes(CAINIAO_REGEX, text)
            "tuxi" -> return this.getTuXiOrder(text)
            "fengchao" -> return this.getFengChaoOrder(text)
            "mama" -> return this.findCodes(MAMA_REGEX, text)
            else -> return this.getOrderGeneral(text)
        }
    }

    private fun getOrderGeneral(text: String): List<String>{
        this.findCodes(CAINIAO_REGEX, text).let {
            if(!it.isEmpty()){
                return it
            }
        }

        var list = this.getFengChaoOrder(text)
        if(!list.isEmpty()){
            return list
        }

        list = this.getTuXiOrder(text)
        if(!list.isEmpty()){
            return list
        }

        this.findCodes(MAMA_REGEX, text).let {
            if(!it.isEmpty()){
                return it
            }
        }

        return listOf()
    }

    private fun getFengChaoOrder(text: String): List<String>{
        val num_list = this.findCodes(FENGCHAO_REGEX, text)
        if(num_list.isEmpty()) return listOf()
        Log.i("OrderAgents","获取到取件码: " + num_list)
        val gui_num = FENGCHAO_GUI_REGEX.findAll(text).map {
            it.groupValues[1].ifEmpty {
                it.groupValues[2]
            }
        }.mapNotNull { this.fix(it) }.toList()
        Log.i("OrderAgents","获取到号码柜: " + gui_num)
        return this.pairCabinet(num_list, gui_num)
    }

    private fun getTuXiOrder(text: String): List<String>{
        val list = this.findCodes(TUXI_REGEX, text).toMutableList()
        val num_list = this.findCodes(TUXI_GUI_REGEX, text)
        if(!num_list.isEmpty()){
            Log.i("OrderAgents","获取到取件码: " + num_list)
            val gui_num = TUXI_GUI_NUMBER_REGEX.findAll(text)
                .map { it.groupValues[1] }
                .mapNotNull { this.fix(it) }
                .toList()
            if(!gui_num.isEmpty()){
                Log.i("OrderAgents","获取到号码柜: " + gui_num)
            }
            list.addAll(this.pairCabinet(num_list, gui_num))
        }
        return list.distinct()
    }

    /**
     * 按顺序把取件码和号柜号配对; 多出来的取件码单独列出。
     *
     * 号柜比取件码多时不配对多出的部分 —— 号柜号本身不是取件码, 单独推送没有意义。
     */
    private fun pairCabinet(codes: List<String>, cabinets: List<String>): List<String>{
        if(cabinets.isEmpty()) return codes
        val list = ArrayList<String>(codes.size)
        val paired = minOf(codes.size, cabinets.size)
        for(i in 0 until paired){
            list.add(cabinets[i] + "号柜" + codes[i])
        }
        for(j in paired until codes.size){
            list.add(codes[j])
        }
        return list
    }

    private fun findCodes(regex: Regex, text: String): List<String>{
        return regex.findAll(text).mapNotNull { this.fix(it.value) }.distinct().toList()
    }

    /** 纠正形近字; 纠正得太多说明匹配到的是英文残片, 丢弃 */
    private fun fix(raw: String): String?{
        val (code, fixed) = OcrText.fixDigits(raw)
        return if(OcrText.tooManyFixes(code, fixed)) null else code
    }
}
