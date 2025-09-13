package com.belltree.pomodoroshareapp.Record

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.belltree.pomodoroshareapp.ui.components.AppTopBar

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    recordViewModel: RecordViewModel,
    onNavigateHome: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Record",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "ここにGeminiのレポートを表示する予定です！！"
            )
        }
    }
}