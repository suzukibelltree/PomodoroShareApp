package com.belltree.pomodoroshareapp

import android.content.Context
import android.os.Bundle
// Compose の Surface を使うため android.view.Surface を削除し material3 を使用
import androidx.compose.material3.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.belltree.pomodoroshareapp.Login.AuthViewModel
import com.belltree.pomodoroshareapp.Login.AuthViewModelFactory
import com.belltree.pomodoroshareapp.MakeSpace.MakeSpaceViewModel
import com.belltree.pomodoroshareapp.MakeSpace.MakeSpaceViewModelFactory
import com.belltree.pomodoroshareapp.Space.SpaceViewModel
import com.belltree.pomodoroshareapp.Space.SpaceViewModelFactory
import com.belltree.pomodoroshareapp.Record.RecordViewModel
import com.belltree.pomodoroshareapp.Record.RecordViewModelFactory
import com.belltree.pomodoroshareapp.Home.HomeViewModel
import com.belltree.pomodoroshareapp.Home.HomeViewModelFactory
import com.belltree.pomodoroshareapp.Setting.SettingViewModel
import com.belltree.pomodoroshareapp.Setting.SettingViewModelFactory
import com.belltree.pomodoroshareapp.domain.repository.AuthRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.UserRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.RecordRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepositoryImpl
import com.belltree.pomodoroshareapp.ui.theme.PomodoroShareAppTheme

object AppContextHolder {
    lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels{
        AuthViewModelFactory(AuthRepositoryImpl(), UserRepositoryImpl())
    }
    private val homeViewModel: HomeViewModel by viewModels{
        HomeViewModelFactory(SpaceRepositoryImpl())
    }
    private val makeSpaceViewModel: MakeSpaceViewModel by viewModels{
        MakeSpaceViewModelFactory(SpaceRepositoryImpl())
    }
    private val recordViewModel: RecordViewModel by viewModels{
        RecordViewModelFactory(RecordRepositoryImpl())
    }
    private val spaceViewModel: SpaceViewModel by viewModels{
        SpaceViewModelFactory(SpaceRepositoryImpl(),RecordRepositoryImpl())
    }
    private val settingViewModel: SettingViewModel by viewModels{
        SettingViewModelFactory(AuthRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PomodoroShareAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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
