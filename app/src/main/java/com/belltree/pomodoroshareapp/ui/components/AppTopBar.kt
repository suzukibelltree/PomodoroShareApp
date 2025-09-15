package com.belltree.pomodoroshareapp.ui.components

import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    rightActionIcons: List<Pair<ImageVector, () -> Unit>> = emptyList(),
    additionalNavigationIcons: List<Pair<ImageVector, () -> Unit>> = emptyList()
) {
    CenterAlignedTopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                if (navigationIcon != null && onNavigationClick != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(navigationIcon, contentDescription = "Back")
                    }
                }
                additionalNavigationIcons.forEach { (icon, onClick) ->
                    IconButton(onClick = onClick) {
                        Icon(icon, contentDescription = null) // Add appropriate descriptions
                    }
                }

            }
        },
        actions = {
            rightActionIcons.forEach { (icon, onClick) ->
                IconButton(onClick = onClick) {
                    Icon(icon, contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)//デフォルトカラーを白
    )
}
