package com.yxmax.orderagents

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxmax.orderagents.GlobalApplication.Companion.liveNotificationManager
import com.yxmax.orderagents.`object`.OrderInfo
import com.yxmax.orderagents.`object`.OrderRepository
import com.yxmax.orderagents.ui.theme.BackgroundLight
import com.yxmax.orderagents.ui.theme.CardLight
import com.yxmax.orderagents.ui.theme.CardPadding
import com.yxmax.orderagents.ui.theme.OrderAgentsTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContentSettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OrderAgentsTheme {
                ContentSettingsScreen(
                    onClick = {
                        finish()
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettingsScreen(onClick: () -> Unit) {

    var selectedOption by remember { mutableStateOf(getNotificationContentType()) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(BackgroundLight),
                title = {
                    Text(
                        text = "更改取餐码实况通知样式",
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
            StandardInfoCard{
                Switch(
                    checked = (selectedOption == 1),
                    onCheckedChange = {
                        if(it){
                            selectedOption = 1
                        } else {
                            selectedOption = 2
                        }
                        switchNotificationContentType(selectedOption)
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
            SimpleInfoCard {
                Switch(
                    checked = (selectedOption == 2),
                    onCheckedChange = {
                        if(it){
                            selectedOption = 2
                        } else {
                            selectedOption = 1
                        }
                        switchNotificationContentType(selectedOption)
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
    }
}

@Composable
fun SimpleInfoCard(onCompose: @Composable () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(CardPadding),

        shape = MaterialTheme.shapes.medium, // 圆角

        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)
    ) {

        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "简洁模式",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            onCompose()
        }

        Row(
            modifier = Modifier
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.mcdonald),
                contentDescription = "icon",
                modifier = Modifier.size(64.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(0.6f)
            ) {
                Text(
                    text = "麦当劳",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "35301",
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
                onClick = {}
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

@Composable
fun StandardInfoCard(onCompose: @Composable () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(CardPadding),

        shape = MaterialTheme.shapes.medium, // 圆角

        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)
    ) {

        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "标准模式",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            onCompose()
        }

        Row(
            modifier = Modifier
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.mcdonald),
                contentDescription = "icon",
                modifier = Modifier.size(64.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(0.6f)
            ) {
                Text(
                    text = "麦当劳",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "35301",
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
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.order_image),
                    contentDescription = "Screenshot",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(0.05f))

            IconButton(
                modifier = Modifier
                    .background(
                        color = Color(208, 208, 208),
                        shape = MaterialTheme.shapes.medium
                    ),
                onClick = {}
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


