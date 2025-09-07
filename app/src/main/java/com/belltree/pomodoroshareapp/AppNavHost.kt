package com.belltree.pomodoroshareapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.belltree.pomodoroshareapp.Home.HomeScreen
import com.belltree.pomodoroshareapp.Home.HomeViewModel
import com.belltree.pomodoroshareapp.Login.AuthScreen
import com.belltree.pomodoroshareapp.Login.AuthViewModel
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
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
    ) {
        composable("login") {
            val viewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                viewModel = viewModel,
                onSignedIn = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                homeViewModel = viewModel,
                onNavigateSettings = { navController.navigate("settings") },
                onNavigateMakeSpace = { navController.navigate("make space") },
                onNavigateRecord = { navController.navigate("record") },
                onNavigateSpace = { id -> navController.navigate("space/$id") },
            )
        }

        composable("make space") {
            val viewModel: MakeSpaceViewModel = hiltViewModel()
            MakeSpaceScreen(
                makeSpaceViewModel = viewModel,
                onNavigateHome = { navController.navigate("home") }
            )
        }

        composable("record") {
            val viewModel: RecordViewModel = hiltViewModel()
            RecordScreen(
                recordViewModel = viewModel,
                onNavigateHome = { navController.navigate("home") }
            )
        }

        composable(
            route = "space/{spaceId}",
            arguments = listOf(navArgument("spaceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: SpaceViewModel = hiltViewModel()
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable

            LaunchedEffect(spaceId) {
                viewModel.getSpaceById(spaceId)
                viewModel.observeSpace(spaceId)
            }

            val currentSpace by viewModel.space.collectAsState()
            currentSpace?.let { target ->
                SpaceScreen(
                    spaceViewModel = viewModel,
                    space = target,
                    onNavigateHome = { navController.navigate("home") }
                )
            }
        }


        composable("settings") {
            val viewModel: SettingViewModel = hiltViewModel()
            SettingScreen(
                settingViewModel = viewModel,
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                },
                onNavigateHome = { navController.navigate("home") }
            )
        }

    }
}