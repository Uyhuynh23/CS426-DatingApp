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
}
