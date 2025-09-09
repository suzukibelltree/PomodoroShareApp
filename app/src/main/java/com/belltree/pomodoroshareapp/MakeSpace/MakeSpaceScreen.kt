package com.belltree.pomodoroshareapp.MakeSpace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.ui.components.AppTopBar


@Composable
@Suppress("unused")
fun MakeSpaceScreen(
    modifier: Modifier = Modifier,
    makeSpaceViewModel: MakeSpaceViewModel,
    onNavigateHome: () -> Unit = {}
) {
    var roomName by remember { mutableStateOf("") }
    var startMinutesText by remember { mutableStateOf("") } // 分単位入力
    var error by remember { mutableStateOf<String?>(null) }
    var isPrivate by remember { mutableStateOf(false) }

    val user = makeSpaceViewModel.user


    Scaffold(
        topBar = {
            AppTopBar(
                title = "MakeSpace",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("部屋の名前") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = startMinutesText,
                onValueChange = { startMinutesText = it.filter { ch -> ch.isDigit() }.take(4) },
                label = { Text("開始時刻を入力してください") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("非公開")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    colors = SwitchDefaults.colors()
                )
            }
            error?.let {
                Text(
                    it,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = {
                    if (roomName.isBlank()) {
                        error = "部屋名を入力してください"
                        return@Button
                    }
                    val minutes = startMinutesText.toLongOrNull() ?: 0L
                    val startTime =
                        if (minutes > 0) System.currentTimeMillis() + minutes * 60_000 else System.currentTimeMillis()
                    makeSpaceViewModel.createSpace(
                        Space(
                            spaceId = "",
                            spaceName = roomName,
                            ownerId = user?.uid ?: "",
                            startTime = startTime,
                            sessionCount = 4,
                            participantsId = emptyList(),
                            createdAt = System.currentTimeMillis(),
                            lastUpdated = System.currentTimeMillis(),
                            isPrivate = isPrivate
                        )
                    )
                    onNavigateHome()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("作成")
            }
        }
    }
}