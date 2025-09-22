package com.belltree.pomodoroshareapp.Setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.LaunchedEffect
import com.belltree.pomodoroshareapp.notification.NoonAlarmScheduler
import com.belltree.pomodoroshareapp.notification.NotificationPermissionManager
import androidx.compose.ui.platform.LocalContext
import jakarta.inject.Inject
import android.os.Build
import android.app.AlarmManager
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

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
                title = "Setting Screen",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        }
    ) {innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "一週間の目標勉強時間を決めよう")
                Text(text = "毎週土曜日にリセットされるよ！")
                OutlinedTextField(
                    value = goalStudyTimeInput,
                    onValueChange = { newValue ->
                        // 数字のみ、または空文字列を許可 (最大長も設定可能)
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                            goalStudyTimeInput = newValue
                        }
                    },
                    label = { Text("目標時間 (例: 5)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (goalStudyTimeInput.isNotBlank()) {
                            settingViewModel.viewModelScope.launch {
                                settingViewModel.updateUserProfiles(
                                    goalStudyTime = goalStudyTimeInput
                                )
                                // Android 12+ では正確なアラームの許可が必要
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                                    if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                                        Toast.makeText(context, "正確なアラームの許可を設定画面で有効にしてください", Toast.LENGTH_LONG).show()
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                }
                                // 目標設定後に12:00のリマインドをスケジュール（許可がなければ近似アラームで設定）
                                NoonAlarmScheduler.cancelDailyNoon(context)
                                NoonAlarmScheduler.scheduleDailyNoon(context)
                            }
                        }
                    },
                ){
                    Text("目標を設定する")
                }
                val currentGoal = settingViewModel.goalStudyTime.value
                if (currentGoal != null) {
                    Text(text = "目標時間: ${currentGoal}")
                }
                Button(onClick = onSignOut) { Text("Sign Out") }
            }
        }
    }
}