package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.belltree.pomodoroshareapp.R
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
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onSpaceClick: (String) -> Unit,
    highlight: Boolean = false
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
                .background(if (highlight) Color(0xFFFFF4E5) else Color(0xFFF3F3F3))//指定しないと自動で色が割り当てられる
                .padding(16.dp)
        ) {
            var derivedState by remember { mutableStateOf(space.spaceState) }
            LaunchedEffect(space.startTime, space.sessionCount) {
                val workDuration = 25 * 60 * 1000L
                val breakDuration = 5 * 60 * 1000L
                val cycleLength = workDuration + breakDuration
                while (true) {
                    val now = System.currentTimeMillis()
                    val timeUntilStart = space.startTime - now
                    if (timeUntilStart > 0) {
                        derivedState = SpaceState.WAITING
                    } else {
                        val elapsed = (now - space.startTime).coerceAtLeast(0L)
                        val currentCycle = (elapsed / cycleLength).toInt()
                        if (currentCycle >= space.sessionCount) {
                            derivedState = SpaceState.FINISHED
                        } else {
                            val cyclePosition = elapsed % cycleLength
                            derivedState = if (cyclePosition < workDuration) SpaceState.WORKING else SpaceState.BREAK
                        }
                    }
                    delay(1000)
                }
            }
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
                    StatusDot(spaceState = derivedState)
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
                        space.participantsId.forEach { participantId ->
                            ParticipantAvatar(
                                participantId = participantId,
                                homeViewModel = homeViewModel
                            )
                        }
                    }
                    }
                }
            }
//            SpaceFooter(createdAt = space.createdAt)
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
private fun ParticipantAvatar(
    participantId: String,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    var url by remember(participantId) { mutableStateOf<String?>(null) }

    LaunchedEffect(participantId) {
        url = if (participantId.isNotBlank()) {
            homeViewModel.getUserById(participantId)?.photoUrl.orEmpty()
        } else {
            ""
        }
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF9C9C9C))
    ) {
        if (url.isNullOrEmpty()) {
            Image(
                painter = painterResource(R.drawable.generic_avatar),
                contentDescription = "デフォルトアバター",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "ユーザーアバター",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                error = painterResource(R.drawable.generic_avatar)
            )
        }
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




