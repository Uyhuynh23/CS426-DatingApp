package com.example.dating.data.model

data class ChatMessage(
    val id: String = "",
    val fromUid: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class DayMessages(
    val dayLabel: String,
    val messages: List<ChatMessage>
)