package com.example.dating.data.model

data class MessagePreview(
    val fromUid: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class ConversationPreview(
    val id: String = "",
    val peer: User = User(),
    val lastMessage: MessagePreview? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val timeAgo: String = "",
    val unreadCount: Int = 0,
    val isTyping: Boolean = false
)