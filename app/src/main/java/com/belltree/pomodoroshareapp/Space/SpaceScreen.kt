package com.belltree.pomodoroshareapp.Space

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.ui.components.AppTopBar

@Composable
fun SpaceScreen(
    modifier: Modifier = Modifier,
    spaceViewModel: SpaceViewModel,
    space: Space,
    onNavigateHome: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Space",
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