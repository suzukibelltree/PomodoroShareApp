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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.belltree.pomodoroshareapp.infra.datastore.IdHistoryStore
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
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var history by remember { mutableStateOf(listOf<String>()) }
                val store = remember(context) { IdHistoryStore(context) }
                LaunchedEffect(Unit) {
                    history = store.historyFlow.first()
                }
                AndroidView(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(53.dp),
                    factory = { ctx ->
                        AutoCompleteTextView(ctx).apply {
                            hint = "IDを入力"
                            setSingleLine(true)
                            imeOptions = EditorInfo.IME_ACTION_DONE
                            threshold = 1
                            val adapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
                            adapter.addAll(history)
                            setAdapter(adapter)
                            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
                            setOnClickListener { showDropDown() }
                            doOnTextChanged { text, _, _, _ ->
                                onInputIdChange(text?.toString() ?: "")
                            }
                            setOnItemClickListener { _, _, position, _ ->
                                val selected = history.getOrNull(position) ?: return@setOnItemClickListener
                                setText(selected)
                                setSelection(selected.length)
                                onInputIdChange(selected)
                            }
                            setOnEditorActionListener { v, actionId, _ ->
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    onInputIdChange(v.text?.toString() ?: "")
                                    true
                                } else false
                            }
                            setText(inputId)
                            setSelection(inputId.length)
                        }
                    },
                    update = { view ->
                        if (view.text?.toString() != inputId) {
                            view.setText(inputId)
                            view.setSelection(inputId.length)
                        }
                        (view.adapter as? ArrayAdapter<String>)?.apply {
                            clear()
                            addAll(history)
                            notifyDataSetChanged()
                        }
                    }
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
                        onClick = {
                            val trimmed = inputId.trim()
                            if (trimmed.isNotEmpty()) {
                                scope.launch {
                                    store.saveId(trimmed)
                                    history = store.historyFlow.first()
                                }
                            }
                            onConfirm()
                        },
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