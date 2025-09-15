package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.ui.theme.PomodoroAppColors

@Composable
fun LabelBar(
    modifier: Modifier = Modifier,
    selectedLabel: SpaceState?,
    onSelectedLabelChange: (SpaceState?) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedLabel == null,
            onClick = { onSelectedLabelChange(null) },
            label = { Text("All") }
        )
        SpaceState.entries.forEach { state ->
            FilterChip(
                selected = selectedLabel == state,
                onClick = { onSelectedLabelChange(state) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = when(state){
                        SpaceState.WAITING -> PomodoroAppColors.SkyBlue
                        SpaceState.WORKING -> PomodoroAppColors.CoralOrange
                        SpaceState.BREAK -> PomodoroAppColors.LimeGreen
                        SpaceState.FINISHED -> PomodoroAppColors.TurquoiseBlue
                    }
                ),
                label = { Text(state.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}