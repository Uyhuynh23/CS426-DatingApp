package com.example.dating.data.model

import com.google.firebase.Timestamp

data class Story(
    val id: String = "",
    val ownerUid: String = "",
    val mediaUrl: String = "",
    val caption: String? = null,
    val createdAt: Timestamp? = null,
    val expiresAt: Long? = null,     // epoch millis (+24h)
    val seenBy: List<String> = emptyList()
)