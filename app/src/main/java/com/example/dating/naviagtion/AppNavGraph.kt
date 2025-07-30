package com.example.dating.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dating.ui.onboarding.OnboardingScreen
import androidx.compose.material3.Text
import com.example.dating.ui.auth.SignUpScreen
import com.example.dating.ui.auth.PhoneNumberScreen
import com.example.dating.ui.auth.VerifyCodeScreen
import com.example.dating.ui.profile.GenderSelectionScreen
import com.example.dating.ui.profile.InterestSelectionScreen
import com.example.dating.ui.profile.SearchFriendScreen
import com.example.dating.ui.profile.EnableNotificationScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.EnableNotification.route
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

        composable(Screen.VerifyCode.route) {
            VerifyCodeScreen(navController)
        }

        composable(Screen.GenderSelect.route) {
            GenderSelectionScreen(navController)
        }

        composable(Screen.InterestSelect.route) {
            // Interest selection screen can be implemented here
            InterestSelectionScreen(navController)
        }

        composable(Screen.SearchFriend.route) {
            SearchFriendScreen(navController)
        }
        composable(Screen.EnableNotification.route) {
            // Enable notification screen can be implemented here
            EnableNotificationScreen(navController)
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


