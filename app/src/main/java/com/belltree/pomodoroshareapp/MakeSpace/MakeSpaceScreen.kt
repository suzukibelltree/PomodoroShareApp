package com.belltree.pomodoroshareapp.MakeSpace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.belltree.pomodoroshareapp.ui.components.AppTopBar

@Composable
fun MakeSpaceScreen(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "MakeSpace",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        }
    ) {innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
        }
    }
}