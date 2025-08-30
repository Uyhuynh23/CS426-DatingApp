package com.example.dating.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object PhoneNumber : Screen("phone_number")
    object VerifyCode : Screen("verify_code")
    object GenderSelect : Screen("gender_select")
    object InterestSelect : Screen("interest_select")
    object SearchFriend : Screen("search_friend")
    object EnableNotification: Screen("enable_notification") // để test sau
    object EmailScreen : Screen("email_screen") // để test sau
    object VerifyEmail : Screen("verify_email") // để test sau
    object Profile : Screen("profile")
    object ProfileDetails : Screen("profile_details") // để test sau
    object Home : Screen("home") // để test sau
    object Favorite : Screen("favorite") // để test sau
    object Match : Screen("match") // để test sau

    object Messages : Screen("messages") // để test sau

    object ChatDetail : Screen("chat_detail/{conversationId}") {
        fun createRoute(conversationId: String) = "chat_detail/$conversationId"
    }

    object UserProfile : Screen("user_profile/{uid}") {
        fun route(uid: String) = "user_profile/$uid"
    }
    object PhotoViewer: Screen("photo_viewer/{startIndex}") {
        fun route(startIndex: Int) = "photo_viewer/$startIndex"
    }
}
