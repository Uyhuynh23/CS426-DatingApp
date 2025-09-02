package com.example.dating.data.model

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthday: String? = null,
    val imageUrl: List<String> = emptyList(),
    val avatarUrl: String? = null,
    val gender: String? = null,
    val job: String? = null,
    val location: String? = null,
    val description: String? = null,
    val interests: List<String> = emptyList(),
    val distance: Int? = null,
    val isOnline: Boolean = false,
    val filterPreferences: UserFilterPreferences? = null
)
