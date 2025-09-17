package com.belltree.pomodoroshareapp.Record

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.R
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun StaticSection() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GeminiAdviceSection()
        DailyWorkDurationGraph()
        AccumulatedWorkDurationGraph()
    }
}

@Composable
fun GeminiAdviceSection() {
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
                    text = "Geminiからのアドバイス\nあああああああああああああああああああああああああああああああ",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun DailyWorkDurationGraph() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Text(
                text = "日別時間",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.ExtraBold
            )
            CartesianChartHost(
                chart =
                    rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(),
                    ),
                model = CartesianChartModel(
                    ColumnCartesianLayerModel.build { series(1, 2, 4, 8, 3, 10, 4) }
                ),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun AccumulatedWorkDurationGraph() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        val modelProducer = remember { CartesianChartModelProducer() }
        LaunchedEffect(Unit) {
            modelProducer.runTransaction {
                lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12) }
            }
        }
        Column {
            Text(
                text = "累積時間",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.ExtraBold
            )
            CartesianChartHost(
                chart =
                    rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(),
                    ),
                modelProducer = modelProducer,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}