package com.yxmax.orderagents

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.`object`.OrderInfo
import com.yxmax.orderagents.`object`.OrderRepository
import com.yxmax.orderagents.ui.theme.BackgroundLight
import com.yxmax.orderagents.ui.theme.CardPadding
import com.yxmax.orderagents.ui.theme.OrderAgentsTheme
import com.yxmax.orderagents.utils.openSettingsActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        setContent {
            OrderAgentsTheme {
                MainScreen(OrderRepository.cardList)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(orderList: Map<Int,OrderInfo>) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background),
                title = {
                    Text(
                        text = "OrderAgents",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                actions = {
                    IconButton(onClick = {
                        openSettingsActivity()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->

        if(orderList.isEmpty()){
            printNoOrder()
        } else {
            printOrderInfo(padding,orderList)
        }
    }
}

@Composable
private fun printNoOrder(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Text(
                text = "暂无通知",
                fontSize = 20.sp,
                color = Color(208,208,208)
            )
        }
    }
}

@Composable
private fun printOrderInfo(padding: PaddingValues, orderList: Map<Int,OrderInfo>){
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        items(
            orderList.values.toList(),
            key = { it.id }
        ) { info ->
            var visible by remember { mutableStateOf(true) }


            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally() + fadeIn() + expandVertically(animationSpec = tween(delayMillis = 300)),
                exit = slideOutHorizontally() + fadeOut() + shrinkVertically(animationSpec = tween(delayMillis = 300))
            ) {
                InfoCard(info) {
                    visible = false // 触发动画
                }

                LaunchedEffect(visible) {
                    if (!visible) {
                        delay(550) // 对应动画时长
                        OrderRepository.removeCardItem(info.id)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(info: OrderInfo, onClick: () -> Unit) {

    val scale: MutableState<Float> = remember { mutableStateOf(1f) }

    val scaleAnim = animateFloatAsState(
        targetValue = scale.value,
        animationSpec = spring(dampingRatio = 0.3f, stiffness = 200f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(CardPadding)
            .graphicsLayer(
                scaleX = scaleAnim.value,
                scaleY = scaleAnim.value
            )
            .pointerInput(Unit) {
                // 禁用Ripple效果，避免点击时产生水波纹
                detectTapGestures(
                    onPress = {
                        // 点击时缩小卡片
                        scale.value = 0.9f

                        // 延时后恢复到原始大小
                        // 使用协程来模拟延时回弹
                        GlobalScope.launch {
                            delay(100)  // 延时 150 毫秒
                            scale.value = 1f
                        }
                    }
                )
            },

        shape = MaterialTheme.shapes.medium, // 圆角

        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = info.image.asImageBitmap(),
                contentDescription = "icon",
                modifier = Modifier.size(64.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.8f)
            ) {
                Text(
                    text = info.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = info.order,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(
                modifier = Modifier
                    .background(
                        color = Color(208, 208, 208),
                        shape = MaterialTheme.shapes.medium
                    ),
                onClick = {
                    liveNotificationManager.cancelLiveNotification(info.id)
                    onClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Check",
                    tint = Color.Black
                )
            }
        }
    }

}