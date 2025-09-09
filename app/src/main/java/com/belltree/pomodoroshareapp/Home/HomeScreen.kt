package com.belltree.pomodoroshareapp.Home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.Space.SpaceViewModel
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.domain.models.TimerState
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import kotlin.String

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
	homeViewModel: HomeViewModel,
    onNavigateSettings: () -> Unit = {},
    onNavigateMakeSpace: () -> Unit = {},
    onNavigateRecord: () -> Unit = {},
	onNavigateSpace: (String) -> Unit = {}
) {

	val spaces by homeViewModel.spaces.collectAsState()
	var keyword by rememberSaveable { mutableStateOf("") }
	val filteredSpaces = if (keyword.isBlank()) spaces else spaces.filter { it.spaceName.contains(keyword, ignoreCase = true) }
	LaunchedEffect(homeViewModel, spaces.isEmpty()){
		if(spaces.isEmpty()){
			homeViewModel.getUnfinishedSpaces()
		}
	}
	Scaffold(
		topBar = {
			AppTopBar(
				title = "Home",
				actionIcons = listOf(
					Icons.Filled.History to onNavigateRecord,
					Icons.Filled.Settings to onNavigateSettings,
				)
			)
		}
	) { innerPadding: PaddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
			) {
				SearchBar(
					keyword = keyword,
					onKeywordChange = { keyword = it }
				)
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(8.dp),
				) {
					items(filteredSpaces) { item ->
						HomeRow(space = item, onSpaceClick = { id -> onNavigateSpace(id) })
					}
				}
			}
			FloatingActionButton(
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(12.dp),
				onClick = {onNavigateMakeSpace()}
			) { Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
		}
	}
}