package com.belltree.pomodoroshareapp.Setting

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.R
import com.belltree.pomodoroshareapp.notification.NoonAlarmScheduler
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.OutlinedTextFieldDefaults


object Variables {
    val StaticLabelLargeLineHeight: TextUnit = 20.sp
    val StaticBodySmallSize: TextUnit = 12.sp
    val StaticBodySmallLineHeight: TextUnit = 16.sp
    val StaticBodySmallTracking: TextUnit = 0.4.sp
    val StaticBodyLargeSize: TextUnit = 16.sp
    val StaticBodyLargeLineHeight: TextUnit = 24.sp
    val StaticBodyLargeTracking: TextUnit = 0.5.sp
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel,
    onSignOut: () -> Unit,
    onNavigateHome: () -> Unit = {}
) {

    val context = LocalContext.current
    var goalStudyTimeInput by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        settingViewModel.loadCurrentUserGoal()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "設定",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding() + 48.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.rectangle6),
                contentDescription = "background image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .width(371.dp)
                    .height(456.dp)
                    .align(Alignment.TopCenter)
                    .alpha(0.7f) // 这里指定透明度，范围 0f ~ 1f
            )




            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .width(371.dp)
                    .height(456.dp) ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "一週間の目標勉強時間を決めよう\n毎週土曜日にリセットされるよ！",
                    fontSize = 16.sp,
                    color = Color(0xFF234121),
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                )


                        OutlinedTextField(
                            value = goalStudyTimeInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                    goalStudyTimeInput = newValue
                                }
                            },
                            label = { Text("目標時間") },
                            placeholder = { Text("例：5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF446E36),
                                unfocusedBorderColor = Color(0xFF446E36),
                                cursorColor = Color(0xFF446E36),
                                focusedLabelColor = Color(0xFF263F1F),
                                unfocusedLabelColor = Color(0xFF263F1F)
                            ),
                            modifier = Modifier
                                .width(300.dp)
                                .padding(horizontal = 16.dp)
                        )


                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = {
                        if (goalStudyTimeInput.isNotBlank()) {
                            settingViewModel.viewModelScope.launch {
                                settingViewModel.updateUserProfiles(
                                    goalStudyTime = goalStudyTimeInput
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val alarmManager =
                                        context.getSystemService(AlarmManager::class.java)
                                    if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                                        Toast.makeText(
                                            context,
                                            "正確なアラームの許可を設定画面で有効にしてください",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val intent =
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                }
                                NoonAlarmScheduler.cancelDailyNoon(context)
                                NoonAlarmScheduler.scheduleDailyNoon(context)
                            }
                        }
                    },
                    modifier = Modifier
                        .width(160.dp)
                        .height(48.dp)
                        .padding(bottom = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6C242)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "目標を設定する",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xF2FFFFFF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                Spacer(modifier = Modifier.height(36.dp))



                val currentGoal = settingViewModel.goalStudyTime.value
                if (currentGoal != null) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = "目標時間：　${currentGoal}　(h)",
                            style = TextStyle(
                                fontSize = 24.sp,
                                lineHeight = Variables.StaticLabelLargeLineHeight,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF234121),
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                        )
                    }
                }


                Spacer(modifier = Modifier.height(100.dp))


                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(129.dp)
                        .height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(81.dp)
                            .height(24.dp)
                    ) {
                        Text(
                            text = "ログアウト",
                            fontSize = 16.sp,
                            style = TextStyle(
//
                                fontWeight = FontWeight(500),
                                color = Color(0xFFE76D48),
                            )
                        )
                    }


                }
            }
        }
    }
}