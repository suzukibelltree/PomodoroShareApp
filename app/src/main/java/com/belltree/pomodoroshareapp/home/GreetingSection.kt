package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeGreetingSection(
    userName: String,
    modifier: Modifier = Modifier
) {
    val messages = listOf(
        "上のラベルを使えば部屋をフィルタリング出来ます！",
        "左上のボタンから統計画面に移動出来ます！",
        "右上のプロフィール画面を押して目標を設定しましょう！"
    )

    val pagerState = rememberPagerState(pageCount = { messages.size })

    Column(modifier = modifier) {
        Text(
            text = "Hi, $userName!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) { page ->
            Text(
                text = messages[page],
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
