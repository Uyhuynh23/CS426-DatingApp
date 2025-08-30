package com.example.dating.data.model

enum class MsgSort {
    NEWEST,      // Sắp xếp theo thời gian mới nhất
    OLDEST,      // Sắp xếp theo thời gian cũ nhất
    UNREAD_FIRST // Ưu tiên tin nhắn chưa đọc
}

data class MessagesFilterState(
    val unreadOnly: Boolean = false,    // Chỉ hiện tin nhắn chưa đọc
    val onlineOnly: Boolean = false,    // Chỉ hiện người dùng đang online
    val onlyMatches: Boolean = false,   // Chỉ hiện người dùng đã match
    val daysBack: Int = 0,             // Số ngày lọc tin nhắn (0 = tất cả)
    val sort: MsgSort = MsgSort.NEWEST  // Mặc định sắp xếp theo thời gian mới nhất
)

fun filterMessages(
    list: List<ConversationPreview>,
    f: MessagesFilterState
): List<ConversationPreview> {
    val now = System.currentTimeMillis()
    val daysInMillis = if (f.daysBack > 0) f.daysBack * 24 * 60 * 60 * 1000L else 0L

    return list
        .asSequence()
        .filter { conversation ->
            // Lọc theo unread
            if (f.unreadOnly && conversation.unreadCount <= 0) return@filter false

            // Lọc theo online status
            if (f.onlineOnly && !conversation.peer.isOnline) return@filter false

            // Lọc theo thời gian
            if (daysInMillis > 0) {
                val messageAge = now - conversation.lastMessageTimestamp
                if (messageAge > daysInMillis) return@filter false
            }

            true
        }
        .let { seq ->
            when (f.sort) {
                MsgSort.NEWEST -> seq.sortedByDescending { it.lastMessageTimestamp }
                MsgSort.OLDEST -> seq.sortedBy { it.lastMessageTimestamp }
                MsgSort.UNREAD_FIRST -> seq.sortedWith(
                    compareByDescending<ConversationPreview> { it.unreadCount > 0 }
                        .thenByDescending { it.lastMessageTimestamp }
                )
            }
        }
        .toList()
}
