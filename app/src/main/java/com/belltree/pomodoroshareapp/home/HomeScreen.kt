package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import kotlin.String
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
	homeViewModel: HomeViewModel,
    onNavigateSettings: () -> Unit = {},
    onNavigateMakeSpace: () -> Unit = {},
    onNavigateRecord: () -> Unit = {},
	onNavigateSpace: (String) -> Unit = {},
) {

	val spaces by homeViewModel.spaces.collectAsState()
	val ownerName by homeViewModel.ownerName.collectAsState() //利用しているユーザー名を指す
	val ownerPhotoUrl by homeViewModel.ownerPhotoUrl.collectAsState()
	val isLoading by homeViewModel.isLoading.collectAsState()
	var keyword by rememberSaveable { mutableStateOf("") }
	var selectedLabel: SpaceState? by rememberSaveable { mutableStateOf<SpaceState?>(null) }
	val filteredSpaces = spaces
		.filter { space ->
			keyword.isBlank() || space.spaceName.contains(keyword, ignoreCase = true) //==Query
		}
		.filter { space ->
			selectedLabel == null || space.spaceState == selectedLabel//==Filter
		}
	LaunchedEffect(homeViewModel, spaces.isEmpty()) {
		if (spaces.isEmpty()) {
			homeViewModel.getUnfinishedSpaces()
		}
		homeViewModel.loadOwner()
	}
	Scaffold(
		topBar = {
			AppTopBar(
				title = "ルーム一覧",
				searchBar = {
					SearchBar(
						keyword = keyword,
						onKeywordChange = { keyword = it },
				)},
				avatarUrl = ownerPhotoUrl.takeIf { it.isNotBlank() },
				onNavigationClick = onNavigateRecord,
				additionalNavigationIcons = listOf(
					Icons.Filled.SignalCellularAlt to onNavigateRecord,
				),
//				rightActionIcons = listOf(
//					Icons.Filled.History to onNavigateRecord,
//				),
				onAvatarClick = onNavigateSettings
			)
		},
		floatingActionButton = {
			FloatingActionButton(
				onClick = { onNavigateMakeSpace() },
				containerColor = Color(0xFFE76D48),
				modifier = Modifier.size(56.dp),
				shape = CircleShape
			) {
				Icon(
					imageVector = Icons.Filled.Groups,
					contentDescription = "Create Room",
					tint = Color(0xFFBEDC53)
				)
			}
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.background(Color.White)//指定しないとスマホデフォルトの色と混ざる
		) {
			LabelBar(
				selectedLabel = selectedLabel,
				onSelectedLabelChange = { selectedLabel = it },
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
			)
			if (isLoading) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator()
				}
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					item() {
						HomeGreetingSection(
							userName = ownerName,
							modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
						)
					}
					items(filteredSpaces) { item ->
						HomeRow(
							space = item,
							onSpaceClick = { id -> onNavigateSpace(id) },
							modifier = Modifier.fillMaxWidth()
						)
					}
				}
			}
		}
	}
}

