package com.yxmax.orderagents.utils

import android.widget.Toast
import androidx.annotation.StringRes
import com.yxmax.orderagents.GlobalApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun sendToast(@StringRes resId: Int){
    val context = GlobalApplication.context
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(
            context,
            context.getString(resId),
            Toast.LENGTH_LONG
        ).show()
    }
}

fun sendToast(s: String){
    val context = GlobalApplication.context
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(
            context,
            s,
            Toast.LENGTH_LONG
        ).show()
    }
}