package com.belltree.pomodoroshareapp.Record

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.R
import com.belltree.pomodoroshareapp.domain.models.DailyStudySummary
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun StaticSection(
    weeklySummary: List<DailyStudySummary>,
    monthlySummary: List<DailyStudySummary>,
    onProgressButtonClick: () -> Unit = {},
    onBackButtonClick: () -> Unit = {},
    weekOffset: Int = 0,
    startOfWeek: LocalDate,
    endOfWeek: LocalDate,
    userGoalStudyTime: Long = 0L,
    message: String = ""
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AdviceSection(
            message = message
        )
        Text(
            text = "目標学習時間: $userGoalStudyTime 時間",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        DailyWorkDurationGraph(
            summary = weeklySummary,
            onProgressButtonClick = onProgressButtonClick,
            onBackButtonClick = onBackButtonClick,
            weekOffset = weekOffset,
            startOfWeek = startOfWeek,
            endOfWeek = endOfWeek,
        )
        MonthlyCumulativeWorkDurationGraph(
            summary = monthlySummary,
            month = YearMonth.now(),
        )
    }
}

@Composable
fun AdviceSection(
    message: String = ""
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.White,
                                    PomodoroAppColors.SkyBlue
                                )
                        )
                )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "App Icon",
                modifier = Modifier.size(48.dp)
            )
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = message.ifEmpty { "継続は力なり！今日も頑張りましょう！" },
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun DailyWorkDurationGraph(
    summary: List<DailyStudySummary>,
    onProgressButtonClick: () -> Unit = {},
    onBackButtonClick: () -> Unit = {},
    weekOffset: Int = 0,
    startOfWeek: LocalDate,
    endOfWeek: LocalDate,
) {
    // summaryが空のときは直近7日分を0で埋める
    val (dateLabels, data) = if (summary.isEmpty()) {
        val last7Days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
        last7Days.map { it.format(DateTimeFormatter.ofPattern("MM/dd")) } to List(7) { 0f }
    } else {
        summary.map { it.date.format(DateTimeFormatter.ofPattern("MM/dd")) } to
                summary.map { it.totalMinutes.toFloat() }
    }

    val weekLabel = "${startOfWeek.format(DateTimeFormatter.ofPattern("MM/dd"))}~${
        endOfWeek.format(DateTimeFormatter.ofPattern("MM/dd"))
    }"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onBackButtonClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous",
                )
            }
            Text(
                text = weekLabel
            )
            IconButton(
                onClick = {
                    onProgressButtonClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Next"
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                Text(
                    text = "日別時間(分)",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.ExtraBold
                )
                CartesianChartHost(
                    chart =
                        rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                valueFormatter = CartesianValueFormatter.Default,
                                size = BaseAxis.Size.Fixed(dateLabels.size.toFloat())
                            ),
                        ),
                    model = CartesianChartModel(
                        ColumnCartesianLayerModel.build {
                            series(*data.toTypedArray())
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dateLabels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .rotate(-30f)
                                .padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyCumulativeWorkDurationGraph(
    summary: List<DailyStudySummary>,
    month: YearMonth = YearMonth.now(),
) {
    val zone = ZoneId.of("Asia/Tokyo")
    val today = LocalDate.now(zone)
    val startOfMonth = month.atDay(1)
    val monthEnd = month.atEndOfMonth()

    // summary が空でなければ summary に含まれる最終日を使い、
    // そうでなければ today と月末のうち早い方を endDate にする
    val summaryLastDate: LocalDate? = summary.maxByOrNull { it.date }?.date
    val endDate: LocalDate = when {
        summaryLastDate != null -> minOf(summaryLastDate, today, monthEnd)
        else -> minOf(today, monthEnd)
    }

    // 日数（startOfMonth 〜 endDate）を計算
    val days = ChronoUnit.DAYS.between(startOfMonth, endDate).toInt() + 1
    val allDates = (0 until days).map { startOfMonth.plusDays(it.toLong()) }

    // ラベルとデータ（時間単位）
    val (dateLabels, data) = if (summary.isEmpty()) {
        val labels = allDates.map { it.format(DateTimeFormatter.ofPattern("MM/dd")) }
        labels to List(days) { 0f }
    } else {
        val summaryMap = summary.associateBy { it.date }
        val dailyHours = allDates.map { date ->
            (summaryMap[date]?.totalMinutes?.toFloat() ?: 0f) / 60f
        }
        // 累積値
        val cumulative = dailyHours.runningFold(0f) { acc, v -> acc + v }.drop(1)
        val labels = allDates.map { it.format(DateTimeFormatter.ofPattern("MM/dd")) }
        labels to cumulative
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${month.monthValue}月 累計作業時間",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.ExtraBold
            )
            // Chart
            val lineColor = Color(0xFF48B3D3)
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider =
                            LineCartesianLayer.LineProvider.series(
                                LineCartesianLayer.rememberLine(
                                    fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                                    areaFill =
                                        LineCartesianLayer.AreaFill.single(
                                            fill(
                                                ShaderProvider.verticalGradient(
                                                    arrayOf(
                                                        lineColor.copy(alpha = 0.4f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                        )
                                )
                            )
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, value, _ ->
                            val i = value.roundToInt().coerceIn(0, dateLabels.size - 1)
                            dateLabels[i]
                        },
                    ),
                ),
                model = CartesianChartModel(
                    LineCartesianLayerModel.build {
                        series(data)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp)
            )
        }
    }
}

