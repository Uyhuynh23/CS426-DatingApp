package com.example.dating.data.model

import com.google.firebase.Timestamp

data class ImageTransform(
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val rotationZ: Float = 0f
)

data class Story(
    val id: String = "",
    val ownerUid: String = "",
    val mediaUrl: String = "",
    val caption: String? = null,
    val createdAt: Timestamp? = null,
    val expiresAt: Long? = null,     // epoch millis (+24h)
    val seenBy: List<String> = emptyList(),
    val imageTransform: ImageTransform? = null  // Store image transformation data
)