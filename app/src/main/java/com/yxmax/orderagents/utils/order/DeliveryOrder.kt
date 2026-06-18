package com.yxmax.orderagents.utils.order

import android.util.Log
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import java.util.Arrays

object DeliveryOrder {

    const val CAINIAO_REGEX = "\\d{1,3}-\\d-\\d{4}"

    const val TUXI_REGEX = "[A-Z][A-Z0-9]-\\d{4,5}|\\d{1,3}-\\d{1,2}-\\d{5}|\\d{1,3}-\\d-\\d{4}|\\b\\d{7,8}\\b|[A-Z]-\\d{1,2}-\\d{5,6}|\\d{4}-\\d{4}"

    const val TUXI_GUI_NUMBER_REGEX = "(?<!\\d)(\\d+)号\\w{0,2}[兔免]喜快递"

    const val TUXI_GUI_REGEX = "(?:(?<=凭|取件码|为)|\\b)[A-Z]?\\d{5,6}\\b"

    const val FENGCHAO_REGEX = "(?:(?<=凭|取件码|码)|\\b)(?:\\d{6,8}|\\d{4}\\s\\d{4})(?!\\d)"

    const val FENGCHAO_GUI_REGEX = "(\\d+)号.*?丰巢|丰巢.*?(\\d+)号"

    const val MAMA_REGEX = "\\d{2}-\\d{3,4}|\\d{1,3}-\\d{1,2}-\\d{2,4}|\\b\\d{5,8}\\b"

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
            "cainiao" -> return Regex(CAINIAO_REGEX).findAll(text).map { it.value }.toList()
            "tuxi" -> return this.getTuXiOrder(text)
            "fengchao" -> return this.getFengChaoOrder(text)
            "mama" -> return Regex(MAMA_REGEX).findAll(text).map { it.value }.toList()
            else -> return this.getOrderGeneral(text)
        }
    }

    private fun getOrderGeneral(text: String): List<String>{
        Regex(CAINIAO_REGEX).findAll(text).map { it.value }.toList().let {
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

        Regex(MAMA_REGEX).findAll(text).map { it.value }.toList().let {
            if(!it.isEmpty()){
                return it
            }
        }

        return listOf()
    }

    private fun getFengChaoOrder(text: String): List<String>{
        val num_list = Regex(FENGCHAO_REGEX).findAll(text).map { it.value }.toMutableList()
        if(!num_list.isEmpty()){
            val list = mutableListOf<String>()
            Log.i("OrderAgents","获取到取件码: " + num_list)
            val gui_num = Regex(FENGCHAO_GUI_REGEX).findAll(text).map {
                it.groupValues[1].ifEmpty {
                    it.groupValues[2]
                }
            }.toMutableList()
            Log.i("OrderAgents","获取到号码柜: " + gui_num)
            if(!gui_num.isEmpty()){
                var count = 0
                for(i in 0 until gui_num.size){
                    list.add(gui_num[i] + "号柜" + num_list[i])
                    count = count + 1
                }
                for(j in count until num_list.size){
                    list.add(num_list[j])
                }
                return list
            }
            for(num in num_list){
                list.add(num)
            }
            return list
        }
        return listOf()
    }

    private fun getTuXiOrder(text: String): List<String>{
        val list = Regex(TUXI_REGEX).findAll(text).map { it.value }.toMutableList()
        val num_list = Regex(TUXI_GUI_REGEX).findAll(text).map { it.value }.toMutableList()
        if(!num_list.isEmpty()){
            Log.i("OrderAgents","获取到取件码: " + num_list)
            val gui_num = Regex(TUXI_GUI_NUMBER_REGEX).findAll(text).map { it.groupValues[1] }.toMutableList()
            if(!gui_num.isEmpty()){
                Log.i("OrderAgents","获取到号码柜: " + gui_num)
                var count = 0
                for(i in 0 until gui_num.size){
                    list.add(gui_num[i] + "号柜" + num_list[i])
                    count = count + 1
                }
                for(j in count until num_list.size){
                    list.add(num_list[j])
                }
            }
        }
        return list
    }
}