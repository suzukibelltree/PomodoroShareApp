package com.belltree.pomodoroshareapp.Contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ContactScreen(
    modifier: Modifier = Modifier,
    contactViewModel: ContactViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateSpace: (String) -> Unit = {}
) {
    val spaces by contactViewModel.spaces.collectAsState()
    val isLoading by contactViewModel.isLoading
    val currentUser by contactViewModel.currentUser
    
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf<SpaceState?>(null) }
    
    val filteredSpaces = spaces
        .filter { space ->
            searchQuery.isBlank() || space.spaceName.contains(searchQuery, ignoreCase = true)
        }
        .filter { space ->
            selectedFilter == null || space.spaceState == selectedFilter
        }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "ルーム一覧",
                actionIcons = listOf(
                    Icons.Filled.Settings to onNavigateSettings,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add navigation to create room */ },
                containerColor = Color(0xFFE76D48),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = "Create Room",
                    tint = Color(0xFFBEDC53)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Greeting Section
            GreetingSection(
                userName = contactViewModel.getUserDisplayName(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Search Bar
            ContactSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Filter Chips
            FilterChipRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Room List
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
                    items(filteredSpaces) { space ->
                        RoomCard(
                            space = space,
                            onRoomClick = { onNavigateSpace(space.spaceId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingSection(
    userName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Hi, ${userName}!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "あああああああああああ\nああああああああああああああああああ\nあああああああああああああああ\nメッセージ！",
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { 
            Text(
                text = "部屋を検索",
                color = Color(0xFF666666),
                fontSize = 16.sp
            )
        },
        leadingIcon = null,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color(0xFF666666)
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(53.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF3F3F3),
            focusedContainerColor = Color(0xFFF3F3F3),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FilterChipRow(
    selectedFilter: SpaceState?,
    onFilterSelected: (SpaceState?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { 
                    Text(
                        "All",
                        color = if (selectedFilter == null) Color(0xFF393939) else Color(0xFF48B3D3)
                    ) 
                },
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color(0xFFC4C4C4),
                    selectedBorderColor = Color(0xFFC4C4C4)
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFFD9D9D9)
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == SpaceState.WAITING,
                onClick = { onFilterSelected(SpaceState.WAITING) },
                label = { 
                    Text(
                        "Waiting",
                        color = Color(0xFF48B3D3)
                    ) 
                },
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color(0xFFC4C4C4),
                    selectedBorderColor = Color(0xFFC4C4C4)
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFFD9D9D9)
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == SpaceState.WORKING,
                onClick = { onFilterSelected(SpaceState.WORKING) },
                label = { 
                    Text(
                        "Working",
                        color = Color(0xFFE76D48)
                    ) 
                },
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color(0xFFC4C4C4),
                    selectedBorderColor = Color(0xFFC4C4C4)
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFFD9D9D9)
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == SpaceState.BREAK,
                onClick = { onFilterSelected(SpaceState.BREAK) },
                label = { 
                    Text(
                        "Break",
                        color = Color(0xFFBEDC53)
                    ) 
                },
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color(0xFFC4C4C4),
                    selectedBorderColor = Color(0xFFC4C4C4)
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFFD9D9D9)
                )
            )
        }
    }
}

@Composable
private fun RoomCard(
    space: Space,
    onRoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onRoomClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F3F3)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Room Name
            Text(
                text = space.spaceName.ifBlank { "部屋名部屋名部屋名部屋名" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF393939),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Divider
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFC4C4C4),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Room Details
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "開始時間：${space.startTime.toDateTimeString()}",
                    fontSize = 15.sp,
                    color = Color(0xFF666666)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDot(spaceState = space.spaceState)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${space.currentSessionCount}/${space.sessionCount} セッション",
                        fontSize = 15.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Text(
                    text = "部屋主：${space.ownerName.ifBlank { "ユーザーA" }}",
                    fontSize = 15.sp,
                    color = Color(0xFF666666)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${space.participantsId.size}人参加中！",
                        fontSize = 15.sp,
                        color = Color(0xFF666666)
                    )
                    
                    // User Avatars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(minOf(3, space.participantsId.size)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF9C9C9C))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.People,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(
    spaceState: SpaceState,
    modifier: Modifier = Modifier
) {
    val dotColor = when (spaceState) {
        SpaceState.WAITING -> Color(0xFF8ECFE3)
        SpaceState.WORKING -> Color(0xFFE76D48)
        SpaceState.BREAK -> Color(0xFFBEDC53)
        else -> Color(0xFF8ECFE3)
    }
    
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(dotColor)
    )
}

private fun Long.toDateTimeString(): String {
    if (this <= 0L) return "2025/8/7 12:23"
    return try {
        val sdf = SimpleDateFormat("yyyy/M/d H:mm", Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        "2025/8/7 12:23"
    }
}
