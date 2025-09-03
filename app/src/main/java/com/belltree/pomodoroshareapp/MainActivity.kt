package com.belltree.pomodoroshareapp

import android.content.Context
import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.belltree.pomodoroshareapp.domain.repository.AuthRepositoryImpl
import com.belltree.pomodoroshareapp.ui.theme.PomodoroShareAppTheme

object AppContextHolder {
    lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels{
        AuthViewModelFactory(AuthRepositoryImpl())
    }
    private val makeSpaceViewModel: MakeSpaceViewModel by viewModels{
        MakeSpaceViewModelFactory(MakeSpaceRepositoryImpl())
    }
    private val recordViewModel: RecordViewModel by viewModels{
        RecordViewModelFactory(RecordRepositoryImpl())
    }
    private val spaceViewModel: SpaceViewModel by viewModels{
        SpaceViewModelFactory(SpaceRepositoryImpl())
    }
    private val settingViewModel: SettingViewModel by viewModels{
        SettingViewModelFactory(SettingRepositoryImpl())
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PomodoroShareAppTheme {
                Surface(modifer = Modifier.fillMaxSize()) {
                    AppNavHost(
                        authViewModel = authViewModel,
                        makeSpaceViewModel = makeSpaceViewModel,
                        recordViewModel = recordViewModel,
                        spaceViewModel = spaceViewModel,
                        settingViewModel = settingViewModel,
                        homeViewModel = homeViewModel
                    )
                }
            }
        }
    }
}
