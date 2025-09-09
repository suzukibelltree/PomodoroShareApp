package com.belltree.pomodoroshareapp.Home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.domain.models.SpaceState

@Composable
fun LabelBar(
    modifier: Modifier = Modifier,
    selectedLabel: SpaceState?,
    onSelectedLabelChange: (SpaceState?) -> Unit,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        FilterChip(
            selected = selectedLabel == null,
            onClick = { onSelectedLabelChange(null) },
            label = { Text("All") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        SpaceState.values().forEach { state ->
            FilterChip(
                selected = selectedLabel == state,
                onClick = { onSelectedLabelChange(state) },
                label = { Text(state.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}