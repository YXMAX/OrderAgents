package com.yxmax.orderagents.`object`

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.yxmax.orderagents.GlobalApplication

data class FactoryInfo (val translate: String, var color: Color, val image: Bitmap){

    constructor(translate: String,color: Color,@DrawableRes id: Int) :
            this(translate,color, ContextCompat.getDrawable(GlobalApplication.context,id)!!.toBitmap())
}

fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return this.bitmap
    }
    // 处理 VectorDrawable 或 AdaptiveIconDrawable 等
    val width = if (this.intrinsicWidth > 0) this.intrinsicWidth else 100
    val height = if (this.intrinsicHeight > 0) this.intrinsicHeight else 100

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bitmap
}

