package com.belltree.pomodoroshareapp.Record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.Space.toLocalDateTime
import com.belltree.pomodoroshareapp.domain.models.Record
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors
import java.time.format.DateTimeFormatter

@Composable
fun RecordSection(
    records: List<Record>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, PomodoroAppColors.SkyBlue),
                )
            )

    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(records.size) { index ->
                val record = records[index]
                RecordItem(record = record)
            }
        }
    }
}


@Composable
fun RecordItem(
    record: Record,
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            val ellipsizedName =
                if (record.roomName == null || record.roomName == "") {
                    "No Room Name"
                } else if (record.roomName.length > 12) {
                    record.roomName.substring(0, 12) + "..."
                } else {
                    record.roomName
                }
            Text(
                text = ellipsizedName,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            HorizontalDivider()
            Text(
                text = "日時:${
                    record.startTime.toLocalDateTime().format(formatter)
                }~${record.endTime.toLocalDateTime().format(formatter)}",
            )
            Text(
                text = "作業時間:${record.durationMinutes}分",
            )
            if (record.taskDescription.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "コメント:")
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        record.taskDescription.forEach { task ->
                            Text(text = "$task")
                        }
                    }
                }
            }

        }
    }
}