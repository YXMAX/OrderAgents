package com.yxmax.orderagents.utils.order

import android.util.Log
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager

object CarplateOrder {

    private const val PROVINCE = "[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领]"

    /** 车牌不使用 I 和 O(避免与 1、0 混淆), 所以这两个位置一定是 OCR 误认 */
    private const val LETTER = "[A-HJ-NP-Za-hj-np-z]"
    private const val TAIL = "[A-HJ-NP-Za-hj-np-z0-9OoIl]"

    /** 车身颜色: 打车软件通常显示在车牌后面, 如 渝AAB5555·白; 含 OCR 可能保留的繁体 */
    private const val COLOR = "香槟|香檳|米|白|黑|银|銀|灰|红|紅|蓝|藍|绿|綠|黄|黃|棕|咖啡|金|橙|紫|粉|青"

    /**
     * 分组: 1=省份 2=发牌机关字母 3=后 5~6 位 4=车身颜色(可选)
     *
     * 颜色后跟「牌」的要排除 —— 「蓝牌」「黄牌」是牌照类型, 不是车身颜色。
     * 后缀的「蓝牌/新能源」等只用于确认这确实是车牌, 不含入结果, 所以必须取分组而不是整个匹配。
     */
    private val PLATE_REGEX = Regex(
        "($PROVINCE)\\s*($LETTER)[·\\-\\s]?($TAIL{5,6})${OcrText.NOT_AFTER}" +
            "(?:[·\\-\\s]*($COLOR)色?(?!牌))?" +
            "(?:[·\\-\\s]*(?:蓝牌|绿牌|黄牌|黑牌|白牌|新能源|纯电|混动|大型|小型|车))?"
    )

    /** 车辆颜色也可能单独成行, 如「白色」「白色 丰田」 */
    private val COLOR_ONLY_REGEX = Regex("(?:^|\\s)($COLOR)色(?:$|\\s)")

    /**
     * 型号行末尾的裸颜色字, 如「吉利银河 银河A7 银」
     *
     * OCR 常吞掉空格(吉利銀河銀河A7银), 所以除了分隔符, 也接受紧跟在数字/字母后的情况 ——
     * 车型代号几乎都以数字或字母结尾(A7/PLUS/Model 3), 这个位置的单个颜色字是颜色而不是车型的一部分。
     * 但不接受紧跟中文, 否则「银河」的「河」这类正常车型字会被误切。
     */
    private val COLOR_TAIL_REGEX = Regex("(?:[·\\-\\s]|(?<=[0-9A-Za-z]))($COLOR)\\s*$")

    /** OCR 有时保留繁体, 统一成简体再展示 */
    private val TRADITIONAL = mapOf(
        '銀' to '银', '車' to '车', '東' to '东', '風' to '风', '龍' to '龙', '馬' to '马',
        '寶' to '宝', '賓' to '宾', '豐' to '丰', '長' to '长', '廣' to '广', '華' to '华',
        '領' to '领', '極' to '极', '藍' to '蓝', '紅' to '红', '綠' to '绿', '黃' to '黄',
        '標' to '标', '緻' to '致', '駿' to '骏', '衛' to '卫', '創' to '创', '謳' to '讴',
        '獅' to '狮', '陸' to '陆', '傑' to '杰', '納' to '纳', '達' to '达', '歐' to '欧',
        '澤' to '泽', '軒' to '轩', '騰' to '腾', '銳' to '锐', '躍' to '跃', '進' to '进'
    )

    /**
     * 车辆品牌, 用于定位型号所在的行
     *
     * 型号本身千变万化(逸动/秦PLUS/Model 3), 没法用正则描述, 但品牌是有限集合 ——
     * 命中品牌就把那一行当型号, 比猜「哪一行像车型」可靠得多。
     */
    private val CAR_BRANDS = arrayOf(
        "长安", "比亚迪", "丰田", "大众", "别克", "现代", "起亚", "日产", "本田", "奥迪",
        "宝马", "奔驰", "特斯拉", "吉利", "荣威", "埃安", "五菱", "雪佛兰", "马自达",
        "福特", "标致", "雪铁龙", "斯柯达", "沃尔沃", "领克", "名爵", "哈弗", "奇瑞",
        "江淮", "传祺", "红旗", "零跑", "小鹏", "蔚来", "理想", "极氪", "深蓝", "阿维塔",
        "岚图", "捷达", "宝骏", "东风", "启辰", "思皓", "欧拉", "几何", "枫叶", "睿蓝",
        "北汽", "广汽", "上汽", "一汽", "smart", "MINI", "捷途", "星途", "魏牌", "极狐",
        "smart", "凯迪拉克", "雷克萨斯", "英菲尼迪", "讴歌", "林肯", "捷豹", "路虎"
    )

    /** 型号行里不该出现的词, 命中说明这行是别的内容 */
    private val MODEL_NOISE = arrayOf("元", "¥", "￥", "公里", "分钟", "取消", "报警", "客服", "评分", "分")

    data class CarInfo(
        /** 带颜色的车牌, 如 渝A·AB5555·白 */
        val plate: String,
        /** 车辆型号, 如 长安逸动新能源; 未识别到为 null */
        val model: String?
    )

    fun createNotification(text: String,pack: String): Boolean{
        val info = this.findCarInfo(text)
        if(info == null) return false
        Log.i("OrderAgents","识别到车牌号: " + info.plate + " 型号: " + info.model)
        liveNotificationManager.createNotification(
            null,
            this.getCarplateFactory(pack),
            info.plate,
            pack,
            2,
            info.model
        )
        return true
    }

    private fun findCarInfo(text: String): CarInfo? {
        for(m in PLATE_REGEX.findAll(text)){
            val (tail, fixed) = OcrText.fixDigits(m.groupValues[3])
            if(OcrText.tooManyFixes(tail, fixed)) continue
            val plate = m.groupValues[1] + m.groupValues[2].uppercase() + "·" + tail.uppercase()
            val model = this.findCarModel(text)
            // 颜色三处来源: 紧跟车牌 -> 型号行末尾的裸颜色字 -> 全文的「X色」
            val color = m.groupValues[4].ifEmpty { this.colorInModelLine(model) }
                .ifEmpty { this.findColor(text) }
            return CarInfo(
                if(color.isEmpty()) plate else plate + "·" + this.simplify(color),
                model?.let { this.stripColorTail(it) }
            )
        }
        return null
    }

    private fun findColor(text: String): String{
        return COLOR_ONLY_REGEX.find(text)?.groupValues?.get(1) ?: ""
    }

    /** 「吉利银河 银河A7 银」这种末尾裸颜色字 */
    private fun colorInModelLine(model: String?): String{
        if(model == null) return ""
        return COLOR_TAIL_REGEX.find(model)?.groupValues?.get(1) ?: ""
    }

    private fun stripColorTail(model: String): String{
        return model.replace(COLOR_TAIL_REGEX, "").trim()
    }

    private fun findCarModel(text: String): String?{
        for(raw in text.lineSequence()){
            val line = this.simplify(raw.trim().trim('|', '.', '、', ','))
            if(line.isEmpty() || line.length > 20) continue
            if(CAR_BRANDS.none { line.contains(it, ignoreCase = true) }) continue
            if(MODEL_NOISE.any { it in line }) continue
            // 颜色可能和型号同行(「白色 丰田卡罗拉」), 去掉颜色前缀
            return line.replace(COLOR_ONLY_REGEX, " ").trim()
        }
        return null
    }

    /** OCR 有时保留繁体(吉利銀河), 统一成简体, 否则品牌表匹配不上 */
    private fun simplify(text: String): String{
        if(text.none { it in TRADITIONAL }) return text
        val sb = StringBuilder(text.length)
        for(c in text) sb.append(TRADITIONAL[c] ?: c)
        return sb.toString()
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
