package com.belltree.pomodoroshareapp.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item{
            FilterChip(
                selected = selectedLabel == null,
                onClick = { onSelectedLabelChange(null) },
                label = {
                    Text(
                        "All",
                        color = if (selectedLabel == null) Color(0xFF393939) else Color(0xFF48B3D3)
                    )
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLabel == null,
                    borderColor = Color(0xFFC4C4C4),
                    selectedBorderColor = Color(0xFFC4C4C4)
                ),
                enabled = true,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFFD9D9D9)
                )
            )
        }
        SpaceState.entries.forEach { state ->
            item{
                FilterChip(
                    selected = selectedLabel == state,
                    onClick = { onSelectedLabelChange(state) },
                    label = {
                        Text(
                            state.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = when(state){
                                SpaceState.WAITING -> PomodoroAppColors.SkyBlue
                                SpaceState.WORKING -> PomodoroAppColors.CoralOrange
                                SpaceState.BREAK -> PomodoroAppColors.LimeGreen
                                SpaceState.FINISHED -> PomodoroAppColors.TurquoiseBlue
                            }
                        )
                    },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedLabel == state,
                        borderColor = Color(0xFFC4C4C4),
                        selectedBorderColor = Color(0xFFC4C4C4)
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.Transparent,
                        selectedContainerColor = Color(0xFFD9D9D9)
                    )
                )
            }
        }
    }
}