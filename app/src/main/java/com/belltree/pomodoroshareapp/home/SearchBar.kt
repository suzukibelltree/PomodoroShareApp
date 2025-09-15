package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
public fun SearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit
)	{
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )	{
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("部屋を検索") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        Text(text = "Hi, ユーザー！", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
        Text(text = "メッセージ！", style = MaterialTheme.typography.bodyMedium)
    }
}