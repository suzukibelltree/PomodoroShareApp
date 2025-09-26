package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors

@Composable
fun UserIconWithFrame(
    userImageURL: String?,
    rewardState: String?,
    modifier: Modifier = Modifier
) {
    val borderBrush = when (rewardState) {
        "Gold" -> Brush.linearGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500)) // 金
        )

        "Silver" -> Brush.linearGradient(
            colors = listOf(Color.LightGray, Color.White) // 銀
        )

        "Bronze" -> Brush.linearGradient(
            colors = listOf(Color(0xFFCD7F32), Color(0xFF8B4513)) // 銅
        )

        "Diamond" -> Brush.linearGradient(
            colors = listOf(Color.Cyan, Color(0xFF00FFFF), Color(0xFF87CEFA)) // ダイヤ
        )

        else -> Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)) // デフォルト
    }

    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .border(BorderStroke(6.dp, borderBrush), CircleShape), // 枠を描画
        contentAlignment = Alignment.Center
    ) {
        if (userImageURL.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                tint = PomodoroAppColors.LightGray,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
        } else {
            AsyncImage(
                model = userImageURL,
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}


