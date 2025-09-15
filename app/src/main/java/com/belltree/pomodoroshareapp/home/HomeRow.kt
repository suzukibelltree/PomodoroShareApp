package com.belltree.pomodoroshareapp.home

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
import androidx.compose.ui.text.font.FontWeight
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        colors = CardDefaults.cardColors(
            containerColor = PomodoroAppColors.LightGray // light gray background
        ),
        onClick = {onSpaceClick(space.spaceId)}
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            SpaceHeader(username = space.ownerName)
            SpaceContent(content = space.spaceName)
            SpaceFooter(createdAt = space.createdAt)
        }
    }
}

@Composable
private fun SpaceHeader(username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = username,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SpaceContent(content: String) {
    Column {
        Text(
            text = content,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SpaceFooter(createdAt: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = formatEpochMillis(createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


//zoneIdは仮
private fun formatEpochMillis(millis: Long, zoneId: ZoneId = ZoneId.of("Asia/Tokyo")): String {
    if (millis <= 0L) return "-"
    return try {
        val instant = Instant.ofEpochMilli(millis)
        val local = instant.atZone(zoneId).toLocalDateTime()
        local.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
    } catch (e: Exception) {
        millis.toString()
    }
}

