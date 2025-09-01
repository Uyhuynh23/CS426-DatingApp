package com.example.dating.navigation

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.dating.ui.mainscreens.FavoriteScreen
import com.example.dating.ui.mainscreens.MatchScreen
import com.example.dating.ui.chat.MessagesScreen
import com.example.dating.ui.profile.GenderSelectionScreen
import com.example.dating.ui.profile.InterestSelectionScreen
import com.example.dating.ui.profile.EnableNotificationScreen
import com.example.dating.ui.profile.ProfileScreen
import com.example.dating.ui.profile.SearchFriendScreen
import com.example.dating.viewmodel.AuthViewModel
import com.example.dating.ui.mainscreens.HomeScreen
import com.example.dating.ui.profile.ProfileDetailsScreen
import com.example.dating.viewmodel.HomeViewModel
import com.example.dating.viewmodel.FavoriteViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.dating.ui.chat.ChatDetailScreen

import androidx.navigation.compose.navigation
import com.example.dating.viewmodel.ProfileViewModel
import com.example.dating.ui.profile.UserProfileScreen
import com.example.dating.ui.profile.PhotoViewerScreen
import com.example.dating.viewmodel.MessagesViewModel
import com.example.dating.viewmodel.ChatViewModel

@Composable
fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel = hiltViewModel()) {

    NavHost(
        navController = navController,
        startDestination = "root_graph"
    ) {
        navigation(startDestination = Screen.Onboarding.route, route = "root_graph") {
            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(navController = navController)
            }

            composable(Screen.Register.route) {
                SignUpScreen(viewModel = authViewModel, navController = navController)
            }

            composable(Screen.PhoneNumber.route) {
                // Phone number screen can be implemented here
                PhoneNumberScreen(navController)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }

            composable(Screen.ProfileDetails.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("root_graph")
                }
                val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
                ProfileDetailsScreen(navController = navController, profileViewModel)
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
            composable(Screen.Home.route) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel(backStackEntry)
                HomeScreen(navController, homeViewModel)
            }

            composable(Screen.Favorite.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("root_graph")
                }
                // Lấy viewModel scoped theo parentEntry
                val favoriteViewModel: FavoriteViewModel = hiltViewModel(parentEntry)
                FavoriteScreen(navController, favoriteViewModel)
            }

            // Match
            composable(
                route = Screen.Match.route + "/{matchedUserId}",
            ) { backStackEntry ->
                val matchedUserId = backStackEntry.arguments?.getString("matchedUserId")
                if (matchedUserId != null) {
                    MatchScreen(navController, matchedUserId)
                } else {
                    Text("No matched user ID provided")
                }
            }

            composable(Screen.Messages.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("root_graph")
                }
                val messageViewModel: MessagesViewModel = hiltViewModel(parentEntry)
                MessagesScreen(navController, viewModel = messageViewModel)
            }

            // Chat detail screen
            composable(
                route = "chat_detail/{conversationId}",
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")!!

                // Scope ChatViewModel theo root_graph
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("root_graph") }
                val chatViewModel: ChatViewModel = hiltViewModel(parentEntry)

                ChatDetailScreen(
                    conversationId = conversationId,
                    navController = navController,
                    viewModel = chatViewModel
                )
            }

            composable(
                route = "user_profile/{uid}",
                arguments = listOf(navArgument("uid") { type = NavType.StringType })
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid")
                UserProfileScreen(navController = navController, userUid = uid)
            }

            // Photo viewer
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
}