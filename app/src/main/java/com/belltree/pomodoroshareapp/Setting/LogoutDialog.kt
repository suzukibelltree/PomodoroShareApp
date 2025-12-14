package com.belltree.pomodoroshareapp.Setting

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "ログアウト") },
        text = { Text(text = "本当にログアウトしますか？") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("はい")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("いいえ")
            }
        }
    )
}