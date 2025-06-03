package com.glowstudio.android.blindsjn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.glowstudio.android.blindsjn.feature.login.view.LoginScreen
import com.glowstudio.android.blindsjn.feature.login.view.SignupScreen

@Composable
fun AuthNavGraph(
    navController: NavHostController,
    onLoginSuccess: (String, String) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    when (navController.currentBackStackEntry?.destination?.route) {
        "login" -> {
            LoginScreen(
                onLoginClick = { phone, password ->
                    onLoginSuccess(phone, password)
                },
                onSignupClick = {
                    navController.navigate("signup")
                },
                onForgotPasswordClick = onForgotPasswordClick
            )
        }
        "signup" -> {
            SignupScreen(
                onSignupClick = { phone, password ->
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                    onLoginSuccess(phone, password)
                },
                onBackToLoginClick = {
                    navController.navigateUp()
                },
                onForgotPasswordClick = onForgotPasswordClick
            )
        }
    }
} 