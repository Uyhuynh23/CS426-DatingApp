package com.example.dating.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object PhoneNumber : Screen("phone_number")
    object Home : Screen("home") // để test sau
}
