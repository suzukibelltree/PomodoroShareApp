package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
public fun SearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    modifier: Modifier = Modifier
)	{
    OutlinedTextField(
        value = keyword,
        onValueChange = onKeywordChange,
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
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//    )	{
//        OutlinedTextField(
//            value = keyword,
//            onValueChange = onKeywordChange,
//            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
//            placeholder = { Text("部屋を検索") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
//        Text(text = "Hi, ユーザー！", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
//        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
//        Text(text = "メッセージ！", style = MaterialTheme.typography.bodyMedium)
//    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun ContactSearchBar(
//    query: String,
//    onQueryChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = query,
//        onValueChange = onQueryChange,
//        placeholder = {
//            Text(
//                text = "部屋を検索",
//                color = Color(0xFF666666),
//                fontSize = 16.sp
//            )
//        },
//        leadingIcon = null,
//        trailingIcon = {
//            Icon(
//                imageVector = Icons.Filled.Search,
//                contentDescription = "Search",
//                tint = Color(0xFF666666)
//            )
//        },
//        modifier = modifier
//            .fillMaxWidth()
//            .height(53.dp),
//        colors = OutlinedTextFieldDefaults.colors(
//            unfocusedContainerColor = Color(0xFFF3F3F3),
//            focusedContainerColor = Color(0xFFF3F3F3),
//            unfocusedBorderColor = Color.Transparent,
//            focusedBorderColor = Color.Transparent
//        ),
//        shape = RoundedCornerShape(16.dp)
//    )
//}