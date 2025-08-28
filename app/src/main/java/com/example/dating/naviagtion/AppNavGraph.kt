package com.example.dating.navigation

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dating.ui.onboarding.OnboardingScreen
import com.example.dating.ui.auth.*
import com.example.dating.ui.profile.*
import com.example.dating.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dating.ui.mainscreens.HomeScreen

@Composable
fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        composable(Screen.Register.route) {
            SignUpScreen(viewModel=authViewModel,navController=navController)
        }

        composable(Screen.PhoneNumber.route) {
            // Phone number screen can be implemented here
            PhoneNumberScreen(navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.ProfileDetails.route) {
            ProfileDetailsScreen(navController = navController)
        }

        composable(Screen.EmailScreen.route) {
            EmailScreen(navController = navController)
        }
        composable(Screen.VerifyEmail.route) {
            VerifyEmailScreen(navController = navController)
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
            LoginScreen(viewModel = authViewModel, navController = navController)
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        // User Profile
        composable(Screen.UserProfile.route) {
            UserProfileScreen(navController = navController)
        }

        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(navArgument("startIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val startIndex = backStackEntry.arguments?.getInt("startIndex") ?: 0
            val images: ArrayList<String> =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<ArrayList<String>>("images") ?: arrayListOf()

            PhotoViewerScreen(
                navController = navController,
                images = images,
                startIndex = startIndex
            )
        }


    }
}


