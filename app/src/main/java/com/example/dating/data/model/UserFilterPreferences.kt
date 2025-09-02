package com.example.dating.data.model

// Represents a user's filter preferences for recommendations
data class UserFilterPreferences(
    val preferredGender: String? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val maxDistance: Int? = null
)

