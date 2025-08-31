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

// 画面遷移をここで管理する
@Composable
fun AppNavHost(modifier: Modifier = Modifier){
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
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
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

    }
}