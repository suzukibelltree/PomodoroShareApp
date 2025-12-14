package com.belltree.pomodoroshareapp.Record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    recordViewModel: RecordViewModel,
    onNavigateHome: () -> Unit = {}
) {
    var showStatic by remember { mutableStateOf(true) }
    val records by recordViewModel.records.collectAsState()
    val weeklySummary by recordViewModel.weeklySummaryForGraph.collectAsState()
    val weekOffset = recordViewModel.currentWeeklyOffset.collectAsState()
    val (startOfWeek, endOfWeek) = recordViewModel.getWeekRangeByOffset(weekOffset.value)
    val zone = ZoneId.of("Asia/Tokyo")
    val startOfWeekDate = Instant.ofEpochMilli(startOfWeek).atZone(zone).toLocalDate()
    val endOfWeekDate = Instant.ofEpochMilli(endOfWeek).atZone(zone).toLocalDate()
    val monthlySummary by recordViewModel.monthlySummaryForGraph.collectAsState()
    val userGoalStudyTime by recordViewModel.goalStudyTime.collectAsState()
    val message by recordViewModel.message.collectAsState()
    LaunchedEffect(Unit) {
        recordViewModel.getAllRecords(recordViewModel.userId)
        recordViewModel.getOneWeekRecords()
        recordViewModel.loadMonthlySummary(YearMonth.now())
        recordViewModel.getUserGoalStudyTimeById(recordViewModel.userId)
        recordViewModel.generateMessage(recordViewModel.userId)
    }
    Scaffold(
        topBar = {
            AppTopBar(
                title = "分析",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    AnalyticsButton(
                        text = "統計",
                        isSelected = showStatic,
                        color = PomodoroAppColors.CoralOrange,
                        onClick = { showStatic = true },
                        modifier = Modifier.width(140.dp)
                    )
                    AnalyticsButton(
                        text = "履歴",
                        isSelected = !showStatic,
                        color = PomodoroAppColors.SkyBlue,
                        onClick = { showStatic = false },
                        modifier = Modifier.width(140.dp)
                    )
                }
                if (showStatic) {
                    StaticSection(
                        weeklySummary = weeklySummary,
                        onProgressButtonClick = { recordViewModel.moveToNextWeek() },
                        onBackButtonClick = { recordViewModel.moveToPreviousWeek() },
                        weekOffset = weekOffset.value,
                        startOfWeek = startOfWeekDate,
                        endOfWeek = endOfWeekDate,
                        monthlySummary = monthlySummary,
                        userGoalStudyTime = userGoalStudyTime,
                        message = message,
                    )
                } else {
                    RecordSection(
                        records = records
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsButton(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = if (isSelected) Color.White else Color.White
        ),
        shape = RoundedCornerShape(
            if (isSelected) 24.dp else 8.dp
        ),
        modifier = modifier
    ) {
        Text(text = text)
    }
}
