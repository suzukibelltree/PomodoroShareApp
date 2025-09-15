package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.domain.models.Space
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun HomeRow(
    space: Space,
    modifier: Modifier = Modifier,
    onSpaceClick: (String) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        onClick = {onSpaceClick(space.spaceId)},
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F3F3))//指定しないと自動で色が割り当てられる
                .padding(16.dp)
        ) {
            SpaceContent(content = space.spaceName)
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFC4C4C4),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "開始時間：${space.startTime.toDateTimeString()}",
                    fontSize = 15.sp,
                    color = Color(0xFF666666)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDot(spaceState = space.spaceState)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${space.currentSessionCount}/${space.sessionCount} セッション",
                        fontSize = 15.sp,
                        color = Color(0xFF666666)
                    )
                }

                SpaceOwnerName(ownerName = space.ownerName)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${space.participantsId.size}人参加中！",
                        fontSize = 15.sp,
                        color = Color(0xFF666666)
                    )

                    // User Avatars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(minOf(3, space.participantsId.size)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF9C9C9C))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.People,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
//            SpaceFooter(createdAt = space.createdAt)
        }
    }
}

@Composable
private fun SpaceOwnerName(ownerName: String) {
    Text(
        text = "部屋主：${ownerName.ifBlank { "ユーザーA" }}",
        fontSize = 15.sp,
        color = Color(0xFF666666)
    )
}

@Composable
private fun SpaceContent(content: String) {
    Column {
        Text(
            text = content.ifEmpty{"部屋名未設定"},
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF393939),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusDot(
    spaceState: SpaceState,
    modifier: Modifier = Modifier
) {
    val dotColor = when (spaceState) {
        SpaceState.WAITING -> Color(0xFF8ECFE3)
        SpaceState.WORKING -> Color(0xFFE76D48)
        SpaceState.BREAK -> Color(0xFFBEDC53)
        else -> Color(0xFF8ECFE3)
    }

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(dotColor)
    )
}

private fun Long.toDateTimeString(): String {
    if (this <= 0L) return "2025/8/7 12:23"
    return try {
        val sdf = SimpleDateFormat("yyyy/M/d H:mm", Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        "2025/8/7 12:23"
    }
}

//@Composable
//private fun SpaceFooter(createdAt: Long) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Start
//    ) {
//        Text(
//            text = formatEpochMillis(createdAt),
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}


//zoneIdは仮
//private fun formatEpochMillis(millis: Long, zoneId: ZoneId = ZoneId.of("Asia/Tokyo")): String {
//    if (millis <= 0L) return "-"
//    return try {
//        val instant = Instant.ofEpochMilli(millis)
//        val local = instant.atZone(zoneId).toLocalDateTime()
//        local.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
//    } catch (e: Exception) {
//        millis.toString()
//    }
//}

