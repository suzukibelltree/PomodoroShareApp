package com.belltree.pomodoroshareapp.makeSpace

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.R
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.TimePicker
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeSpaceScreen(
    modifier: Modifier = Modifier,
    makeSpaceViewModel: MakeSpaceViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigateSpace: (String) -> Unit = {},
) {
    var roomName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isPrivate by remember { mutableStateOf(false) }
    val zone = ZoneId.of("Asia/Tokyo") // ユーザーが入力したタイムゾーン
    var userInputDateTime by remember { mutableStateOf(LocalDateTime.now(zone)) } //ユーザーの指定したタイムゾーンの現在時刻

    val timeState = rememberTimePickerState(
        initialHour = userInputDateTime.hour,
        initialMinute = userInputDateTime.minute,
        is24Hour = true
    )
    var showTimeInputDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = userInputDateTime.atZone(zone).toInstant().toEpochMilli(),
        initialDisplayMode = DisplayMode.Picker
    )
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var user by remember { mutableStateOf<User?>(null) }
    var sessionCountInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        user = makeSpaceViewModel.getCurrentUserById()
    }

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
            // 部屋名入力
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = {
                    Text(
                        text = "部屋名",
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFF393939),
                            letterSpacing = 0.4.sp,
                        )
                    )
                },
                supportingText = {
                    if (roomName.isBlank()) {
                        Text(
                            text = "部屋名にキーワードを入れてみましょう。 \n" +
                                    "部屋が検索されやすくなります。  \n" +
                                    "例：【資格勉強】【大学生】一緒に勉強しましょう",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFF9C9C9C),
                                letterSpacing = 0.5.sp,
                            )
                        )
                    }
                },
                trailingIcon = {
                    if (roomName.isNotBlank()) {
                        Icon(
                            Icons.Outlined.Cancel,
                            contentDescription = "Clear room name",
                            modifier = Modifier
                                .padding(1.dp)
                                .size(24.dp)
                        )
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF393939)
                ),
                modifier = Modifier
                    .width(343.dp)
                    .padding(0.dp)
                    .align(Alignment.CenterHorizontally)
            )
            val fieldWidth = 343.dp


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = userInputDateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { showDatePickerDialog = true },
                    modifier = Modifier
                        .width(123.dp)
                        .height(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF48B3D3),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 0.dp)
                ) {
                    Text(
                        text = "日付を設定する",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight(500),
                            color = Color(0xF2FFFFFF),
                            letterSpacing = 0.1.sp,
                        ),
                        modifier = Modifier
                            .width(99.dp)
                            .height(20.dp)
                    )
                }
            }

            if (showDatePickerDialog) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                val selectedDateTime = Instant.ofEpochMilli(selectedDateMillis)
                                    .atZone(zone)
                                    .toLocalDateTime()
                                userInputDateTime = LocalDateTime.of(
                                    selectedDateTime.year,
                                    selectedDateTime.monthValue,
                                    selectedDateTime.dayOfMonth,
                                    userInputDateTime.hour,
                                    userInputDateTime.minute
                                )
                            }
                            showDatePickerDialog = false
                        }) {
                            Text(
                                "OK",
                                color = Color(0xFFE76D48) // OKボタン
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerDialog = false }) {
                            Text(
                                "キャンセル",
                                color = Color(0xFF9C9C9C) // キャンセルボタン
                            )
                        }
                    },
                    // 背景色を指定
                    colors = DatePickerDefaults.colors(
                        containerColor = Color(0xFFFFFFFF),
                        headlineContentColor = Color(0xFF393939),
                        weekdayContentColor = Color(0xFF393939),
                        subheadContentColor = Color(0xFF393939),
                        yearContentColor = Color(0xFF393939),
                        currentYearContentColor = Color(0xFF393939),
                        selectedYearContentColor = Color(0xFFFFFFFF),
                        selectedYearContainerColor = Color(0xFF48B3D3),
                        dayContentColor = Color(0xFF393939),
                        disabledDayContentColor = Color(0xFF9C9C9C),
                        selectedDayContentColor = Color(0xFFFFFFFF),
                        selectedDayContainerColor = Color(0xFF48B3D3),
                        todayContentColor = Color(0xFF48B3D3),
                        todayDateBorderColor = Color(0xFF48B3D3)
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color(0xFFFFFFFF),
                            headlineContentColor = Color(0xFF393939),
                            weekdayContentColor = Color(0xFF393939),
                            subheadContentColor = Color(0xFF393939),
                            yearContentColor = Color(0xFF393939),
                            currentYearContentColor = Color(0xFF393939),
                            selectedYearContentColor = Color(0xFFFFFFFF),
                            selectedYearContainerColor = Color(0xFF48B3D3),
                            dayContentColor = Color(0xFF393939),
                            disabledDayContentColor = Color(0xFF9C9C9C),
                            selectedDayContentColor = Color(0xFFFFFFFF),
                            selectedDayContainerColor = Color(0xFF48B3D3),
                            todayContentColor = Color(0xFF48B3D3),
                            todayDateBorderColor = Color(0xFF48B3D3)
                        )
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = userInputDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { showTimeInputDialog = true },
                    modifier = Modifier
                        .width(123.dp)
                        .height(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF48B3D3),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 0.dp)
                ) {
                    Text(
                        text = "時間を設定する",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight(500),
                            color = Color(0xF2FFFFFF),
                            letterSpacing = 0.1.sp,
                        ),
                        modifier = Modifier
                            .width(99.dp)
                            .height(20.dp)
                    )
                }
            }

            if (showTimeInputDialog) {
                AlertDialog(
                    onDismissRequest = { showTimeInputDialog = false },
                    containerColor = Color(0xFFFFFFFF),
                    confirmButton = {
                        TextButton(
                            onClick = {
                                userInputDateTime = LocalDateTime.of(
                                    userInputDateTime.year,
                                    userInputDateTime.monthValue,
                                    userInputDateTime.dayOfMonth,
                                    timeState.hour,
                                    timeState.minute
                                )
                                showTimeInputDialog = false
                            }
                        ) {
                            Text("OK", color = Color(0xFFE76D48))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimeInputDialog = false }) {
                            Text("キャンセル", color = Color(0xFF9C9C9C))
                        }
                    },
                    text = {
                        TimePicker(
                            state = timeState,
                            colors = androidx.compose.material3.TimePickerDefaults.colors(
                                clockDialColor = Color.White,
                                clockDialSelectedContentColor = Color.White,
                                clockDialUnselectedContentColor = Color(0xFF393939),
                                selectorColor = Color(0xFF48B3D3),
                                containerColor = Color(0xFFFFFFFF),
                                periodSelectorBorderColor = Color.Transparent,
                                periodSelectorSelectedContainerColor = Color(0xFF48B3D3),
                                periodSelectorUnselectedContainerColor = Color.White,
                                periodSelectorSelectedContentColor = Color.White,
                                periodSelectorUnselectedContentColor = Color(0xFF393939),
                                timeSelectorSelectedContainerColor = Color(0xFF48B3D3),
                                timeSelectorUnselectedContainerColor = Color.White,
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContentColor = Color(0xFF393939)
                            )

                        )
                    }
                )
            }



            //セッション数
            OutlinedTextField(
                value = sessionCountInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                        sessionCountInput = newValue
                    }
                },
                label = {
                    Text(
                        text = "セッション数",
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFF393939),
                            letterSpacing = 0.4.sp,
                        )
                    )
                },
                supportingText = {
                    if (sessionCountInput.isBlank()) {
                        Text(
                            text = "セッション数を入力してください",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFF9C9C9C),
                                letterSpacing = 0.5.sp,
                            )
                        )
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF393939)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(343.dp)
                    .padding(0.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // 非公開
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = "非公開アイコン",
                    modifier = Modifier
                        .size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("非公開")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    enabled = true,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF48B3D3),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .width(52.dp)
                        .height(32.dp)
                        .padding(start = 4.dp, top = 2.dp, end = 4.dp, bottom = 2.dp)
                )
            }
            error?.let {
                Text(
                    it,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.fillMaxWidth().height(120.dp))

            // 部屋を作成する
            Button(
                onClick = {
                    if (roomName.isBlank()) {
                        error = "部屋名を入力してください"
                        return@Button
                    }
                    if (sessionCountInput.isBlank()) {
                        error = "セッション数を入力してください"
                        return@Button
                    }
                    // ユーザー入力 (ローカル時刻 + タイムゾーン) を UTC エポックミリ秒に変換
                    val startTime = userInputDateTime
                        .atZone(zone)
                        .toInstant()
                        .toEpochMilli()
                    val newSpace = Space(
                        spaceId = "",
                        spaceName = roomName,
                        ownerId = user?.userId ?: "01",
                        ownerName = user?.userName ?: "ゲスト",
                        startTime = startTime,
                        sessionCount = sessionCountInput.toInt(),
                        participantsId = emptyList(),
                        createdAt = System.currentTimeMillis(),
                        lastUpdated = System.currentTimeMillis(),
                        isPrivate = isPrivate
                    )
                    // 作成後のIDを取得してルームへ遷移
                    coroutineScope.launch {
                        val id = makeSpaceViewModel.createSpaceReturnId(newSpace)
                        onNavigateSpace(id)
                    }
                },
                modifier = Modifier
                    .width(161.dp)
                    .height(56.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(color = Color(0xFFE76D48), shape = RoundedCornerShape(size = 100.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE76D48),
                    contentColor = Color.White

                )
            ) {
                Text(
                    text = "部屋を作成する",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,

                        fontWeight = FontWeight(500),
                        color = Color(0xF2FFFFFF),
                        letterSpacing = 0.15.sp,
                    ),
                    modifier = Modifier
                        .width(113.dp)
                        .height(24.dp)
                )
            }
        }
    }
}