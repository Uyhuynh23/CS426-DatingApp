package com.example.dating.data.model

import java.util.Date

class User {
}

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthday: Date = Date(),
    val bio: String = "",
    val avatarUrl: String = "",
    val interests: List<String> = emptyList()
)