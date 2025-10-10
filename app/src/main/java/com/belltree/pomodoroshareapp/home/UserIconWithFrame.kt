package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.belltree.pomodoroshareapp.R
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors

@Composable
fun UserIconWithFrame(
    userImageURL: String?,
    rewardState: String?,
    modifier: Modifier = Modifier
) {
    // 報酬ランクに応じたフレーム画像を選択
    val frameRes = when (rewardState) {
        "Diamond" -> R.drawable.diamond
        "Gold" -> R.drawable.gold
        "Silver" -> R.drawable.silver
        else -> R.drawable.bronze
    }

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // 下層：ユーザーアイコン
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

        // 上層：透過PNGのフレームを重ねる
        Image(
            painter = painterResource(id = frameRes),
            contentDescription = "Frame",
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit
        )
    }
}


