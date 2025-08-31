package com.belltree.pomodoroshareapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.belltree.pomodoroshareapp.ui.theme.PomodoroShareAppTheme

@Composable
fun PomodoroShareApp(){
    PomodoroShareAppTheme {
        Scaffold { innerPadding ->
            AppNavHost(modifier = Modifier.padding(innerPadding))

        }
    }
}