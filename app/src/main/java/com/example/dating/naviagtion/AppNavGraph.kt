package com.example.dating.navigation

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dating.ui.onboarding.OnboardingScreen
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dating.ui.auth.SignUpScreen
import com.example.dating.ui.auth.PhoneNumberScreen
import com.example.dating.ui.auth.VerifyCodeScreen
import com.example.dating.ui.auth.VerifyEmailScreen
import com.example.dating.ui.auth.EmailScreen
import com.example.dating.ui.chat.MessagesScreen
import com.example.dating.ui.profile.GenderSelectionScreen
import com.example.dating.ui.profile.InterestSelectionScreen
import com.example.dating.ui.profile.EnableNotificationScreen
import com.example.dating.ui.profile.ProfileScreen
import com.example.dating.ui.profile.SearchFriendScreen
import com.example.dating.viewmodel.AuthViewModel
import com.example.dating.ui.mainscreens.HomeScreen
import com.example.dating.ui.profile.ProfileDetailsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.dating.ui.chat.ChatDetailScreen


@Composable
fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel = hiltViewModel()) {
    val authViewModel = hiltViewModel<AuthViewModel>()
    val messageViewModel = hiltViewModel<com.example.dating.viewmodel.MessagesViewModel>()
    NavHost(
        navController = navController,
        startDestination = Screen.Messages.route
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

        composable(Screen.Messages.route) {
            MessagesScreen(navController, viewModel = messageViewModel)
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")!!
            ChatDetailScreen(conversationId = conversationId, navController = navController)
        }

    }
}
