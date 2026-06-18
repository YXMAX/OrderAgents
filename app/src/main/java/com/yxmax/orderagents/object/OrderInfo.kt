package com.yxmax.orderagents.`object`

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

data class OrderInfo (
    val screenshot: Uri?,
    val image: Bitmap,
    val title: String,
    val order: String,
    val id: Int,
    val type: Int
)

object OrderRepository {

    private val _cardList = mutableStateMapOf<Int,OrderInfo>()
    val cardList: SnapshotStateMap<Int,OrderInfo> = _cardList

    fun addCardItem(item: OrderInfo) {
        _cardList.put(item.id, item)
    }

    fun removeCardItem(id: Int) {
        _cardList.remove(id)
    }

    fun canCleanCache(): Boolean{
        return _cardList.size <= 1
    }

    fun getOrder(id: Int): String?{
        val get = _cardList.get(id)
        if(get != null){
            return get.order
        }
        return null
    }

    fun getScreenshot(id: Int): Uri?{
        val get = _cardList.get(id)
        if(get != null){
            return get.screenshot
        }
        return null
    }

    fun hasOrder(order: String): Boolean{
        return _cardList.any { it.value.order.equals(order) }
    }

    fun getNextId(): Int{
        var id = 1001
        while(id<=1005){
            if(_cardList.containsKey(id)){
                id = id + 1
                continue
            }
            break
        }
        return id
    }
}