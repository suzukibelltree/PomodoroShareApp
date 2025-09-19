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

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel,
    onSignOut: () -> Unit,
    onNavigateHome: () -> Unit = {}
) {

    var goalStudyTimeInput by remember { mutableStateOf("") }

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
                            }
                        }
                    },
                ){
                    Text("目標を設定する")
                }
                Button(onClick = onSignOut) { Text("Sign Out") }
            }
        }
    }
}