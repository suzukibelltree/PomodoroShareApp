package com.belltree.pomodoroshareapp.Space

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.belltree.pomodoroshareapp.R
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@Composable
fun SpaceScreen(
    modifier: Modifier = Modifier,
    spaceViewModel: SpaceViewModel,
    space: Space,
    onNavigateHome: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // ← 整个画面背景纯白
    ) {
        // 这里写 SpaceScreen 的实际内容
    }

    val comments = spaceViewModel.comments.collectAsState().value
    var user by remember { mutableStateOf<User?>(null) }
    val participantsName = spaceViewModel.userNames.collectAsState().value
    val participants = spaceViewModel.participants.collectAsState().value
    val timeUntilStart by spaceViewModel.timeUntilStartMillis.collectAsState()
    val progress by spaceViewModel.progress.collectAsState()
    val remainingTime by spaceViewModel.remainingTimeMillis.collectAsState()
    val currentSessionCount by spaceViewModel.currentSessionCount.collectAsState()
    val spaceState by spaceViewModel.spaceState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val lifecycleObserver = remember {
        AppLifecycleObserver(spaceViewModel)
    }

    LaunchedEffect(Unit) {
        user = spaceViewModel.getCurrentUserById()
    }

    // 参加者リストが変更されたときにユーザー名と参加者情報を再取得する
    LaunchedEffect(space.participantsId) {
        spaceViewModel.fetchUserNames(space.participantsId)
        spaceViewModel.fetchParticipants(space.participantsId)
    }

    DisposableEffect(lifecycleOwner) {
        lifecycleObserver.updateSpaceScreenActive(true)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    BackHandler {
        showDialog = true
    }


    Scaffold(
        topBar = {
            AppTopBar(
                title = space.spaceName,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { showDialog = true },
                rightActionIcons = listOf(
                    Icons.Default.ContentCopy to {
                        clipboardManager.setText(AnnotatedString(space.spaceId))
                        Toast.makeText(
                            context,
                            "スペースIDをコピーしました",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            // スペースの開始時刻表示（開始1時間前より前の場合のみ表示）
            val oneHourInMillis = 60 * 60 * 1000
            val secondUntilStart = timeUntilStart / 1000
            val minutes = secondUntilStart / 60
            val seconds = secondUntilStart % 60
            if (spaceState == SpaceState.WAITING && timeUntilStart > oneHourInMillis) {
                val startLabel = formatDateTimeForLabel(space.startTime)
                Text(
                    text = "開始時刻\n $startLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    textAlign = TextAlign.Center
                )
            } else if (spaceState == SpaceState.WAITING) { // 開始1時間前以内の場合はカウントダウン表示
                Text(
                    text = "開始まであと\n%02d分%02d秒".format(minutes, seconds),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(Modifier.height(48.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                TimerCircle(
                    radius = 110.dp,
                    strokeWidth = 6.dp,
                    progress = progress,
                    progressColor =
                        when (spaceState) {
                            SpaceState.WAITING -> PomodoroAppColors.SkyBlue
                            SpaceState.WORKING -> PomodoroAppColors.CoralOrange
                            SpaceState.BREAK -> PomodoroAppColors.LimeGreen
                            SpaceState.FINISHED -> PomodoroAppColors.LimeGreen
                        },
                    trackColor = Color.LightGray,
                    remainingTimeMillis = remainingTime,
                    spaceState = spaceState,
                    currentSessionCount = currentSessionCount,
                    sessionCount = space.sessionCount
                )
            }

            Spacer(Modifier.height(16.dp))

            var selectedTab by remember { mutableStateOf(0) }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF3F3F3),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF48B3D3),
                        height = 3.dp
                    )
                }
            ) {                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("コメント") },
                    selectedContentColor = Color(0xFF393939),
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("参加者") },
                    selectedContentColor = Color(0xFF393939),
                    unselectedContentColor = Color.Gray
                )
            }

            if (selectedTab == 0) {
                user?.let { currentUser ->
                    CommentSection(
                        comments = comments,
                        spaceViewModel = spaceViewModel,
                        user = currentUser,
                        spaceId = space.spaceId
                    )
                }
            } else {
                ParticipantSection(
                    participants = participants,
                )
            }
        }
    }
    if (showDialog) {
        ConfirmDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                spaceViewModel.markRecentlyLeft(space.spaceId)
                onNavigateHome(space.spaceId)
            }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun TimerCircle(
    radius: Dp,
    strokeWidth: Dp,
    progress: Float,
    progressColor: Color,
    trackColor: Color,
    remainingTimeMillis: Long,
    spaceState: SpaceState,
    currentSessionCount: Int,
    sessionCount: Int
) {
    val sizePx = radius * 2
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis) % 60
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(sizePx)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawCircle(color = trackColor, style = stroke)
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (spaceState) {
                    SpaceState.WAITING -> "待機中"
                    SpaceState.WORKING -> "作業中"
                    SpaceState.BREAK -> "休憩中"
                    SpaceState.FINISHED -> "終了"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (spaceState == SpaceState.WORKING || spaceState == SpaceState.BREAK) {
                Text(
                    text = "現在のセッション：${currentSessionCount}/${sessionCount}",
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            } else {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CommentSection(
    comments: List<Comment>,
    spaceViewModel: SpaceViewModel,
    user: User,
    spaceId: String
) {
    val listState = rememberLazyListState()

    // コメントが追加されるたびにスクロール
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(Color(0xFFF3F3F3))) {
            LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = false) {
                items(comments) { comment ->
                    CommentRow(
                        comment = comment,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            var input by remember { mutableStateOf("") }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("コメントを入力") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Spacer(Modifier.size(8.dp))
            IconButton(
                onClick = {
                    if (input.isNotEmpty()) {
                        spaceViewModel.addComment(
                            spaceId = spaceId,
                            Comment(
                                spaceId = spaceId,
                                userId = user.userId,
                                userName = user.userName,
                                photoUrl = user.photoUrl,
                                content = input,
                                postedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    input = ""
                }
            ) { Text("▶", color = Color(0xFF285D9D), fontSize = 18.sp) }
        }
    }
}

@Composable
private fun ParticipantSection(participants: List<User>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(Color(0xFFF3F3F3))) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(participants) { participant -> ParticipantRow(participant = participant) }
            }
        }
    }
}

@Composable
private fun ParticipantRow(participant: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (participant.photoUrl.isNotEmpty()) {
            AsyncImage(
                model = participant.photoUrl,
                contentDescription = "参加者アバター",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.generic_avatar)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.generic_avatar),
                contentDescription = "デフォルトアバター",
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = participant.userName,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Unspecified
        )
    }
}

private fun formatDateTimeForLabel(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--/--/-- --:--"
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    return sdf.format(Date(epochMillis))
}