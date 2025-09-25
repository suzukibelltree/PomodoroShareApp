package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors

@Composable
fun InputIDDialog(
    modifier: Modifier,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onInputIdChange: (String) -> Unit,
    inputId: String,
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "IDを入力してください",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputId,
                    onValueChange = onInputIdChange,
                    placeholder = {
                        Text(
                            text = "IDを入力",
                            color = Color(0xFF666666),
                            fontSize = 16.sp
                        )
                    },
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
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text(
                            text = "ホーム画面に戻る",
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { onConfirm() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PomodoroAppColors.CoralOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("決定する")
                    }
                }
            }
        }
    }

}