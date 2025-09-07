package com.belltree.pomodoroshareapp.Space

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.domain.models.Comment


@Composable
fun CommentRow(
    modifier: Modifier = Modifier,
    comment: Comment,
) {
    CommentCard(
        modifier, comment
    )
}

@Composable
private fun CommentCard(
    modifier: Modifier = Modifier,
    comment: Comment
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(Modifier.padding(16.dp)) {
            CommentHeader(username = comment.userName)
            CommentContent(content = comment.content, images = emptyList())
        }
    }
}

@Composable
private fun CommentHeader(username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = username,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CommentContent(content: String, images: List<String>?) {
    Column {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = if (images.isNullOrEmpty()) 12.dp else 12.dp)
        )
    }
}
