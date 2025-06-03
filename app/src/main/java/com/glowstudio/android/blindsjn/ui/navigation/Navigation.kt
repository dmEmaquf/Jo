package com.glowstudio.android.blindsjn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.feature.main.view.MainScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            AuthNavGraph(
                navController = navController,
                onLoginSuccess = { phone, password ->
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    // TODO: 비밀번호 찾기 화면 구현
                }
            )
        }

        composable("signup") {
            AuthNavGraph(
                navController = navController,
                onLoginSuccess = { phone, password ->
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    // TODO: 비밀번호 찾기 화면 구현
                }
            )
        }

        composable("main") {
            MainScreen()
        }
    }
}
