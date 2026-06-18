package com.yxmax.orderagents

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxmax.orderagents.ui.theme.BackgroundLight

class AppSettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppSettingsScreen(
                onClick = {
                    finish()
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(onClick: () -> Unit) {

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(BackgroundLight),
                title = {
                    Text(
                        text = "自动识别应用设置",
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
                .verticalScroll(rememberScrollState())
        ) {
            BigCard {
                AppSettingCard(image = R.drawable.weixin, packageName = "com.tencent.mm", title = "微信")
                AppSettingCard(image = R.drawable.zfb, packageName = "com.eg.android.AlipayGphone", title = "支付宝")
                AppSettingCard(image = R.drawable.mcdonald, packageName = "com.mcdonalds.gma.cn", title = "麦当劳")
                AppSettingCard(image = R.drawable.kfc, packageName = "com.yek.android.kfc.activitys", title = "KFC")
                AppSettingCard(image = R.drawable.luckin, packageName = "com.lucky.luckyclient", title = "瑞幸咖啡")
                AppSettingCard(image = R.drawable.cainiao, packageName = "com.cainiao.wireless", title = "菜鸟")
                AppSettingCard(image = R.drawable.didi, packageName = "com.sdu.didi.psnger", title = "滴滴出行")
                AppSettingCard(image = R.drawable.haluo, packageName = "com.jingyao.easybike", title = "哈啰")
                AppSettingCard(image = R.drawable.huaxz, packageName = "com.huaxiaozhu.rider", title = "花小猪打车")
            }
        }
    }
}

@Composable
fun AppSettingCard(image: Int,packageName: String,title: String){

    var checked by remember { mutableStateOf(GlobalApplication.packageList.contains(packageName)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 18.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = "icon",
            modifier = Modifier.size(58.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                switchAppList(packageName,checked)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(32,108,255),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(210,210,210),
                uncheckedBorderColor = Color.White
            ),

        )
    }
}


