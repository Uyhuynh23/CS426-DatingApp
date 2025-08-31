package com.example.dating.data.model

data class ConversationPreview(
    val id: String = "",
    val peer: User = User(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val timeAgo: String = "",
    val unreadCount: Int = 0,
    val isTyping: Boolean = false
)