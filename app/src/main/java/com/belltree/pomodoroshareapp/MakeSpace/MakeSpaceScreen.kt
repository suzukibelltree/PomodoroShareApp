package com.belltree.pomodoroshareapp.MakeSpace
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import java.time.LocalDateTime
import java.time.ZoneId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeSpaceScreen(
    modifier: Modifier = Modifier,
    makeSpaceViewModel: MakeSpaceViewModel,
    onNavigateHome: () -> Unit = {}
) {
    var roomName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isPrivate by remember { mutableStateOf(false) }
    val zone = ZoneId.of("Asia/Tokyo") // ユーザーが入力したタイムゾーン
    var userInput by remember { mutableStateOf(LocalDateTime.now(zone)) } //ユーザーの指定したタイムゾーンの現在時刻
    val timeState = rememberTimePickerState(is24Hour = true) //時計のホップアップと紐づいてる
    var showDialog by remember { mutableStateOf(false) }
    val user = makeSpaceViewModel.user

    Scaffold(
        topBar = {
            AppTopBar(
                title = "MakeSpace",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("部屋の名前") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                listOf(15, 25, 45).forEach { m ->
                    AssistChip(onClick = {
                        timeState.minute = m % 60 // 必要に応じ丸め
                    }, label = { Text("${m}分") })
                }
            }

            // 表示用フィールド + ダイアログ
            OutlinedTextField(
                value = "%02d:%02d".format(timeState.hour, timeState.minute),
                onValueChange = {/*ホップアップで変数は変更されるのでここでかえるひつようなし*/},
                readOnly = true,
                trailingIcon = {
                    IconButton({ showDialog = true }) { Icon(Icons.Default.AccessTime, null) }
                }
            )

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton({
                            // TimePicker の選択値を LocalDateTime に反映
                            userInput = LocalDateTime.of(
                                userInput.year,
                                userInput.monthValue,
                                userInput.dayOfMonth,
                                timeState.hour,
                                timeState.minute
                            )
                            showDialog = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton({ showDialog = false }) { Text("キャンセル") } },
                    text = { TimePicker(state = timeState) }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("非公開")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    colors = SwitchDefaults.colors()
                )
            }
            error?.let {
                Text(
                    it,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = {
                    if (roomName.isBlank()) {
                        error = "部屋名を入力してください"
                        return@Button
                    }
                    // ユーザー入力 (ローカル時刻 + タイムゾーン) を UTC エポックミリ秒に変換
                    val startTime = userInput
                        .atZone(zone)
                        .toInstant()
                        .toEpochMilli()
                    makeSpaceViewModel.createSpace(
                        Space(
                            spaceId = "",
                            spaceName = roomName,
                            ownerId = user?.uid ?: "",
                            startTime = startTime,
                            sessionCount = 4,
                            participantsId = emptyList(),
                            createdAt = System.currentTimeMillis(),
                            lastUpdated = System.currentTimeMillis(),
                            isPrivate = isPrivate
                        )
                    )
                    onNavigateHome()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("作成")
            }
        }
    }
}