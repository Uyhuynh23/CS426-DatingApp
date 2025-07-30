package com.example.dating.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dating.ui.onboarding.OnboardingScreen
import androidx.compose.material3.Text
import com.example.dating.ui.auth.SignUpScreen
import com.example.dating.ui.auth.PhoneNumberScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        composable(Screen.Register.route) {
            SignUpScreen(navController)
        }

        composable(Screen.PhoneNumber.route) {
            // Phone number screen can be implemented here
            PhoneNumberScreen(navController)
        }
        // Login
        composable(Screen.Login.route) {
            Text("Login Screen - Test only")
        }

        // Home
        composable(Screen.Home.route) {
            Text("Home Screen - Test only")
        }
    }
}


