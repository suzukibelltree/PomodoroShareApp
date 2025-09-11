package com.belltree.pomodoroshareapp.Space

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SpaceScreen(
    modifier: Modifier = Modifier,
    spaceViewModel: SpaceViewModel,
    space: Space,
    onNavigateHome: () -> Unit = {}
) {

    val comments = spaceViewModel.comments.collectAsState().value
    var user by remember { mutableStateOf<User?>(null) }
    val participantsName = spaceViewModel.userNames.collectAsState().value
    val ownerName = spaceViewModel.ownerName.collectAsState().value
    val timeUntilStart by spaceViewModel.timeUntilStartMillis.collectAsState()
    val progress by spaceViewModel.progress.collectAsState()
    val isRunning by spaceViewModel.isRunning.collectAsState()
    val remainingTime by spaceViewModel.remainingTimeMillis.collectAsState()
    val currentSessionCount by spaceViewModel.currentSessionCount.collectAsState()
    val spaceState by spaceViewModel.spaceState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val lifecycleObserver = remember {
        AppLifecycleObserver(spaceViewModel)
    }

    LaunchedEffect(Unit) {
        user = spaceViewModel.getCurrentUserById()
    }

    // 参加者リストが変更されたときにユーザー名を再取得
    LaunchedEffect(space.participantsId) {
        spaceViewModel.fetchUserNames(space.participantsId)
    }

    DisposableEffect(lifecycleOwner) {
        lifecycleObserver.updateSpaceScreenActive(true)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }


    Scaffold(
        topBar = {
            AppTopBar(
                title = space.spaceName,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            val startLabel = formatDateTimeForLabel(space.startTime)
            Text(
                text = "開始予定時刻\n $startLabel",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )
            if (timeUntilStart != null) {
                // 開始前カウントダウン表示
                Text("開始まであと ${timeUntilStart!! / 1000} 秒")
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
                    progressColor = Color(0xFF285D9D),
                    trackColor = Color(0xFFBBD0EE),
                    remainingTimeMillis = remainingTime
                )
            }
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
                textAlign = TextAlign.Center
            )
            if (spaceState == SpaceState.WORKING || spaceState == SpaceState.BREAK) {
                Text(
                    text = "現在のセッション：${currentSessionCount}/${space.sessionCount}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(20.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(8.dp))

            var selectedTab by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFFE6E6E6)) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("コメント") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("参加者") }
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
                    participantsName = participantsName,
                    ownerName = ownerName
                )
            }
        }
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
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
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
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFE6E6E6))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = false) {
                items(comments) { comment ->
                    CommentRow(
                        comment = comment,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            IconButton(onClick = {
                if (input.isNotEmpty()) {
                    spaceViewModel.addComment(
                        spaceId = spaceId,
                        Comment(
                            spaceId = spaceId,
                            userId = user.userId,
                            userName = user.userName,
                            content = input,
                            postedAt = System.currentTimeMillis()
                        )
                    )
                }
                input = ""
            }) {
                Text("▶", color = Color(0xFF285D9D), fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun ParticipantSection(participantsName: List<String>, ownerName: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFE6E6E6))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(participantsName) { name ->
                    ParticipantRow(name = name, isOwner = name == ownerName)
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(name: String, isOwner: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isOwner) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "部屋主",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Spacer(modifier = Modifier.width(24.dp)) // アイコン分のスペースを空ける
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isOwner) Color(0xFF285D9D) else Color.Unspecified // 部屋主は青色
        )
    }
}

private fun formatDateTimeForLabel(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--/--/-- --:--"
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    return sdf.format(Date(epochMillis))
}