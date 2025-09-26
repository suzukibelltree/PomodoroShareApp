package com.belltree.pomodoroshareapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.home.UserIconWithFrame
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    rightActionIcons: List<Pair<ImageVector, () -> Unit>> = emptyList(),
    additionalNavigationIcons: List<Pair<ImageVector, () -> Unit>> = emptyList(),
    avatarUrl: String? = null,
    userRewardState: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    searchBar: (@Composable () -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = {
            if (searchBar != null) {
                searchBar()
            } else {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = PomodoroAppColors.DarkGray,
                )
            }
        },
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                additionalNavigationIcons.forEach { (icon, onClick) ->
                    IconButton(onClick = onClick) {
                        Icon(icon, contentDescription = null, tint = PomodoroAppColors.DarkGray)
                    }
                }
                if (navigationIcon != null && onNavigationClick != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            navigationIcon,
                            contentDescription = "Back",
                            tint = PomodoroAppColors.DarkGray
                        )
                    }
                }
            }
        },
        actions = {
            if (!avatarUrl.isNullOrBlank()) {
                IconButton(onClick = { onAvatarClick?.invoke() }) {
                    UserIconWithFrame(
                        userImageURL = avatarUrl,
                        rewardState = userRewardState.toString(),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(avatarUrl)
//                            .crossfade(true)
//                            .build(),
//                        contentDescription = "Avatar",
//                        modifier = Modifier
//                            .size(32.dp)
//                            .clip(CircleShape)
//                    )
                }
            } else if (title == "ルーム一覧") {//ホーム画面のみデフォルトアイコンを表示
                IconButton(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    onClick = { onAvatarClick?.invoke() }
                ) {
//                    Icon(
//                        Icons.Filled.Person,
//                        contentDescription = "Avatar",
//                        tint = PomodoroAppColors.DarkGray
//                    )
                    UserIconWithFrame(
                        userImageURL = null,
                        rewardState = userRewardState.toString(),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                }
            }
            rightActionIcons.forEach { (icon, onClick) ->
                IconButton(onClick = onClick) {
                    Icon(icon, contentDescription = null, tint = PomodoroAppColors.DarkGray)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            scrolledContainerColor = Color.White
        )//デフォルトカラーを白
    )
}
