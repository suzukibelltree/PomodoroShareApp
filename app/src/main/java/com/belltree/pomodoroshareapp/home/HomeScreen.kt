package com.belltree.pomodoroshareapp.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.ui.components.AppTopBar

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
    val ownerRewardState by homeViewModel.ownerRewardState.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    var keyword by rememberSaveable { mutableStateOf("") }
    var selectedLabel: SpaceState? by rememberSaveable { mutableStateOf<SpaceState?>(null) }
    val recentlyLeftSpaceId by homeViewModel.recentlyLeftSpaceId.collectAsState()
    val pinnedSpace by homeViewModel.selectedSpace.collectAsState()
    var showSpaceDialog by remember { mutableStateOf(false) }
    var showIdDialog by remember { mutableStateOf(false) }
    var inputId by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val filteredSpaces = spaces
        .filter { space ->
            keyword.isBlank() || space.spaceName.contains(keyword, ignoreCase = true) //==Query
        }
        .filter { space ->
            selectedLabel == null || space.spaceState == selectedLabel//==Filter
        }
        //非公開部屋を非表示にする
        .filter { space ->
            !space.isPrivate
        }
        .let { list ->
            val idx = list.indexOfFirst { it.spaceId == recentlyLeftSpaceId }
            if (idx >= 0) {
                val target = list[idx]
                listOf(target) + list.filterIndexed { i, _ -> i != idx }
            } else list
        }
    LaunchedEffect(Unit) {
        // 画面表示のたびに最新の未終了スペース一覧を取得
        homeViewModel.load()
        homeViewModel.loadOwner()
    }

    LaunchedEffect(recentlyLeftSpaceId) {
        // 退出直後にも一覧を再取得して先頭に並び替えられるようにする
        homeViewModel.load()
        // フィルターで非表示になっている可能性があるためクリアする
        selectedLabel = null
        // 検索キーワードで非表示になっている可能性があるためクリアする
        keyword = ""
        // 一覧に存在しない場合に備えて対象スペースを明示的に取得
        recentlyLeftSpaceId?.let { id ->
            val trimmed = id.trim()
            if (trimmed.isNotEmpty()) {
                Log.w("HomeScreen", "fetch pinned space id=${'$'}trimmed")
                homeViewModel.getSpaceById(trimmed)
            }
        }
        // 一番上にスクロールしてハイライトを見せる
        if (recentlyLeftSpaceId != null && filteredSpaces.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }
    Scaffold(
        topBar = {
            AppTopBar(
                title = "ルーム一覧",
                searchBar = {
                    SearchBar(
                        keyword = keyword,
                        onKeywordChange = { keyword = it },
                    )
                },
                avatarUrl = ownerPhotoUrl.takeIf { it.isNotBlank() },
                userRewardState = ownerRewardState.toString(),
                onNavigationClick = onNavigateRecord,
                additionalNavigationIcons = listOf(
                    Icons.Filled.SignalCellularAlt to onNavigateRecord,
                    Icons.Filled.Search to { showIdDialog = true }
                ),
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
                    imageVector = Icons.Filled.Add,
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = listState
                ) {
                    item() {
                        HomeGreetingSection(
                            userName = ownerName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    // 退出した部屋を最上部に固定表示（一覧に無い場合でも表示）
                    val topSpace = filteredSpaces.firstOrNull { it.spaceId == recentlyLeftSpaceId }
                        ?: pinnedSpace
                    if (topSpace != null) {
                        item("pinned-${'$'}{topSpace.spaceId}") {
                            HomeRow(
                                space = topSpace,
                                onSpaceClick = { id -> onNavigateSpace(id) },
                                modifier = Modifier.fillMaxWidth(),
                                highlight = topSpace.spaceId == recentlyLeftSpaceId
                            )
                        }
                    }
                    val rest = topSpace?.let { pinned ->
                        filteredSpaces
                            .filter { it.spaceId != pinned.spaceId }
                    } ?: filteredSpaces
                        .filter { it.isPrivate == false }//非公開部屋を非表示にする
                    items(rest) { item ->
                        HomeRow(
                            space = item,
                            onSpaceClick = { id -> onNavigateSpace(id) },
                            modifier = Modifier.fillMaxWidth(),
                            highlight = item.spaceId == recentlyLeftSpaceId
                        )
                    }
                }
            }
        }
        if (showIdDialog) {
            InputIDDialog(
                modifier = modifier,
                onDismiss = { showIdDialog = false },
                onConfirm = {
                    showIdDialog = false
                    showSpaceDialog = true
                },
                onInputIdChange = { inputId = it },
                inputId = inputId,
            )
        }

        if (showSpaceDialog) {
            LaunchedEffect(showSpaceDialog, inputId) {
                // ダイアログ表示時に対象スペースを取得（suspend 呼び出しはここで行う）
                val trimmed = inputId.trim()
                if (trimmed.isNotEmpty()) {
                    homeViewModel.getSpaceById(trimmed)
                }
            }
            pinnedSpace?.let { sp ->
                ShowSpaceDialog(
                    onDismiss = { showSpaceDialog = false },
                    onConfirm = { id ->
                        showSpaceDialog = false
                        onNavigateSpace(id)
                    },
                    space = sp
                )
            }
        }
    }
}

