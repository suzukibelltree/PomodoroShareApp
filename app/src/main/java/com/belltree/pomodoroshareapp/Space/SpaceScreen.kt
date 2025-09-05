package com.belltree.pomodoroshareapp.Space

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpaceScreen(
    modifier: Modifier = Modifier,
    spaceViewModel: SpaceViewModel,
    space: Space,
    onNavigateHome: () -> Unit = {}
) {

    val comments = spaceViewModel.comments.collectAsState().value

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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            val startLabel = formatDateTimeForLabel(space.startTime)
            Text(
                text = "開始予定時刻: $startLabel",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                TimerCircle(
                    radius = 110.dp,
                    strokeWidth = 6.dp,
                    progressColor = Color(0xFF285D9D),
                    trackColor = Color(0xFFBBD0EE),
                    timeText = "25:00"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* play/pause */ }) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play")
                }
                IconButton(onClick = { /* reset */ }) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset")
                }
            }

            Spacer(Modifier.height(8.dp))

            var selectedTab by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selectedTab) {
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

            Spacer(Modifier.height(8.dp))

            if (selectedTab == 0) {
                CommentSection(
                    comments = comments,
                    spaceViewModel = spaceViewModel
                )
            } else {
                ParticipantSection()
            }
        }
    }
}

@Composable
private fun TimerCircle(
    radius: Dp,
    strokeWidth: Dp,
    progressColor: Color,
    trackColor: Color,
    timeText: String,
) {
    val sizePx = radius * 2
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(sizePx)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawCircle(color = trackColor, style = stroke)
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Timer", fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(text = timeText, fontSize = 20.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CommentSection(
    comments: List<Comment>,
    spaceViewModel: SpaceViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFE6E6E6))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(comments){comment ->
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
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(8.dp))
            IconButton(onClick = {
                if (input.isNotEmpty()) {
                    spaceViewModel.addComment(
                        spaceId = spaceViewModel.spaceID,
                        Comment(

                        )
                    )
                }
            }) {
                Text("▶", color = Color(0xFF285D9D), fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun ParticipantSection() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFE6E6E6))
        ) {}
    }
}

private fun formatDateTimeForLabel(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--/--/-- --:--"
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    return sdf.format(Date(epochMillis))
}