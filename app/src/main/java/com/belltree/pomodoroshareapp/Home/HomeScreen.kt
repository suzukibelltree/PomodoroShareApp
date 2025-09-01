package com.belltree.pomodoroshareapp.Home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
		){
			FloatingActionButton(
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(12.dp),
				onClick = { /*TODO*/ }
			) { Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
		}
	}
}