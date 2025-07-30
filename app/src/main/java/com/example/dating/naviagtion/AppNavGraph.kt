package com.example.dating.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dating.ui.onboarding.OnboardingScreen
import androidx.compose.material3.Text

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
<<<<<<< Updated upstream
        startDestination = Screen.Onboarding.route
=======
        startDestination = Screen.Profile.route

>>>>>>> Stashed changes
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        // Login
        composable(Screen.Login.route) {
            Text("Login Screen - Test only")
        }

        // Register
        composable(Screen.Register.route) {
            Text("Register Screen - Test only")
        }

        // Home
        composable(Screen.Home.route) {
            Text("Home Screen - Test only")
        }
    }
}


