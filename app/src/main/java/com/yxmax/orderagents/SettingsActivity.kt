package com.yxmax.orderagents

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DateRange
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxmax.orderagents.ui.theme.BackgroundLight
import com.yxmax.orderagents.ui.theme.CardPadding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.core.content.ContextCompat
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.ui.theme.CardLight
import com.yxmax.orderagents.utils.createShortCut
import com.yxmax.orderagents.utils.openAccessibilitySettings
import com.yxmax.orderagents.utils.openAppSettings
import com.yxmax.orderagents.utils.openStorageAccess
import com.yxmax.orderagents.utils.openUsageAccess
import com.yxmax.orderagents.utils.sendToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingsScreen(
                onClick = {
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onClick: () -> Unit) {


    val context = GlobalApplication.context

    // 权限申请器
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    // 检查通知权限函数
    fun checkNotificationPermission(onPermissionGranted: () -> Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
            PackageManager.PERMISSION_GRANTED -> {

                // 检查 安卓16+ 和 魅族实况通知
                if (Build.VERSION.SDK_INT >= 36 && !Build.MANUFACTURER.equals("meizu")) {
                    val nm = context.getSystemService(android.app.NotificationManager::class.java)
                    if (!nm.canPostPromotedNotifications()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.live_notification_not_enabled),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                // 所有权限都满足
                onPermissionGranted()
            } else -> {
                // 请求通知权限
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // 检查通知权限函数
    fun checkBatteryPermission(onPermissionGranted: () -> Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
            PackageManager.PERMISSION_GRANTED -> {
                // 所有权限都满足
                onPermissionGranted()
            } else -> {
            // 请求通知权限
            notificationPermissionLauncher.launch(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            }
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(BackgroundLight),
                title = {
                    Text(
                        text = "设置",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {

                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            BigCard {
                SettingCard(icon = Icons.Rounded.DateRange, title = "开启通知") {
                    checkNotificationPermission {
                        sendToast(R.string.live_notification_enabled)
                    }
                }
                SettingCard(icon = Icons.Rounded.Lock, title = "开启忽略电池优化") {
                    checkBatteryPermission {
                        sendToast(R.string.battery_optimization_enabled)
                    }
                }
                SettingCard(icon = Icons.Rounded.Info, title = "开启开机自启和后台运行") {
                    openAppSettings()
                }
                SettingCard(icon = Icons.Rounded.Refresh, title = "开启读取存储权限") {
                    openStorageAccess()
                }
                SettingCard(icon = Icons.Rounded.Email, title = "开启应用使用情况访问权限") {
                    openUsageAccess()
                }
                SettingCard(icon = Icons.AutoMirrored.Rounded.List, title = "开启无障碍") {
                    openAccessibilitySettings()
                }
            }

            BigCard {
                SettingCardWithDescription(icon = Icons.Default.Favorite, title = "创建截图快捷方式", description = "用于应用小窗截图") {
                    createShortCut()
                }
            }

            BigCard {
                SettingCard(icon = Icons.Default.Build, title = "实况通知测试") {
                    liveNotificationManager.showLiveNotificationExample()
                }
            }
        }
    }
}

@Composable
fun BigCard(onCompose: @Composable () -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CardPadding),

        shape = MaterialTheme.shapes.medium,

        colors = CardDefaults.cardColors(CardLight)
    ) {
        onCompose()
    }
}

@Composable
fun SettingCard(icon: ImageVector,title: String,onClick: () -> Unit){

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 24.dp, vertical = 24.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
}

@Composable
fun SettingCardWithDescription(icon: ImageVector,title: String,description: String,onClick: () -> Unit){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 24.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column() {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = description,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}