package com.belltree.pomodoroshareapp.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Contacts
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
    onNavigateContact: () -> Unit = {}
) {

	val spaces by homeViewModel.spaces.collectAsState()
	var keyword by rememberSaveable { mutableStateOf("") }
	var selectedLabel: SpaceState? by rememberSaveable { mutableStateOf<SpaceState?>(null) }
	val filteredSpaces = spaces
		.filter { space ->
			keyword.isBlank() || space.spaceName.contains(keyword, ignoreCase = true)
		}
		.filter { space ->
			selectedLabel == null || space.spaceState == selectedLabel
		}
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
					Icons.Filled.Contacts to onNavigateContact,
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
				LabelBar(
					selectedLabel = selectedLabel,
					onSelectedLabelChange = { selectedLabel = it }
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
			)	{ Icon(imageVector = Icons.Filled.People, contentDescription = null) }
		}
	}

@Composable
private fun HomeRow(
	space: com.belltree.pomodoroshareapp.domain.models.Space,
	onSpaceClick: (String) -> Unit
)	{
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp, vertical = 6.dp)
			.clickable { onSpaceClick(space.spaceId) },
		colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F3))
	)	{
		Column(modifier = Modifier.padding(12.dp))	{
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = space.spaceName.ifBlank { "部屋名" },
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					modifier = Modifier.weight(1f),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
				StatusDot(state = space.spaceState)
				Text(text = "開始時間：${space.startTime.toDateTimeOrDash()}", style = MaterialTheme.typography.bodySmall)
			}
			Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
			Text(text = "${space.currentSessionCount}/${space.sessionCount} セッション", style = MaterialTheme.typography.bodySmall)
			Spacer(modifier = androidx.compose.ui.Modifier.height(2.dp))
			Text(text = "部屋主：${space.ownerName.ifBlank { "ユーザーA" }}", style = MaterialTheme.typography.bodySmall)
			Spacer(modifier = androidx.compose.ui.Modifier.height(2.dp))
			Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
				Text(text = "${space.participantsId.size}人参加！", style = MaterialTheme.typography.bodySmall)
				Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
					Icon(imageVector = Icons.Filled.People, contentDescription = null)
				}
			}
		}
	}
}

@Composable
private fun StatusDot(state: com.belltree.pomodoroshareapp.domain.models.SpaceState) {
	androidx.compose.foundation.layout.Box(
		modifier = Modifier
			.width(8.dp)
			.height(8.dp)
	)
}

private fun Long.toDateTimeOrDash(): String {
	if (this <= 0L) return "-"
	return try {
		val sdf = java.text.SimpleDateFormat("yyyy/M/d H:mm", java.util.Locale.getDefault())
		sdf.format(java.util.Date(this))
	} catch (e: Exception) { "-" }
}
