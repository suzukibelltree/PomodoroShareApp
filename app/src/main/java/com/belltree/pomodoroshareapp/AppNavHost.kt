package com.belltree.pomodoroshareapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.belltree.pomodoroshareapp.Login.AuthScreen
import com.belltree.pomodoroshareapp.Login.AuthViewModel
import com.belltree.pomodoroshareapp.Home.HomeScreen
import com.belltree.pomodoroshareapp.Home.HomeViewModel
import com.belltree.pomodoroshareapp.MakeSpace.MakeSpaceScreen
import com.belltree.pomodoroshareapp.MakeSpace.MakeSpaceViewModel
import com.belltree.pomodoroshareapp.Record.RecordScreen
import com.belltree.pomodoroshareapp.Record.RecordViewModel
import com.belltree.pomodoroshareapp.Setting.SettingScreen
import com.belltree.pomodoroshareapp.Setting.SettingViewModel
import com.belltree.pomodoroshareapp.Space.SpaceScreen
import com.belltree.pomodoroshareapp.Space.SpaceViewModel

// 画面遷移をここで管理する
@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    makeSpaceViewModel: MakeSpaceViewModel,
    recordViewModel: RecordViewModel,
    spaceViewModel: SpaceViewModel,
    settingViewModel: SettingViewModel,
    homeViewModel: HomeViewModel,
){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
    ){
        composable("login") {
            AuthScreen(
                viewModel = authViewModel,
                onSignedIn = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateSettings = { navController.navigate("settings") },
                onNavigateMakeSpace = { navController.navigate("make space") },
                onNavigateRecord = { navController.navigate("record") },
                onNavigateSpace = { navController.navigate("space") }
            )
        }

        composable("make space") {
            MakeSpaceScreen(
                makeSpaceViewModel = makeSpaceViewModel,
                onNavigateHome = { navController.navigate("home") }
            )
        }

        composable("record") {
            RecordScreen(
                onNavigateHome = { navController.navigate("home") }
            )
        }

        composable("space") {
            SpaceScreen(
                onNavigateHome = { navController.navigate("home") }
            )
        }

        composable("settings") {
            SettingScreen(
                settingViewModel = settingViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                },
                onNavigateHome = { navController.navigate("home") }
            )
        }

    }
}