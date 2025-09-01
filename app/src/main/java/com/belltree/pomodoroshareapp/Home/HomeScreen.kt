package com.belltree.pomodoroshareapp.Home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.belltree.pomodoroshareapp.ui.components.AppTopBar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateSettings: () -> Unit = {}
) {
	Scaffold(
		topBar = {
			AppTopBar(
				title = "Home",
				actionIcons = listOf(
					Icons.Filled.Settings to onNavigateSettings
				)
			)
		}
	) {innerPadding ->
		Column(
			modifier = modifier
				.fillMaxSize()
				.padding(innerPadding),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Text("Home", style = MaterialTheme.typography.headlineMedium)
		}
	}
}